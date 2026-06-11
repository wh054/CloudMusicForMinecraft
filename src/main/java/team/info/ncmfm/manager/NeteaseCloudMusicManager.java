package team.info.ncmfm.manager;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.NcmConfig;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.entity.*;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.QrStatus;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NeteaseCloudMusicManager implements IMusicManager {
    private static final Logger logger = LogManager.getLogger(NeteaseCloudMusicManager.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36";
    private static final String CACHE_COOKIE = "Cookie";
    private static final String CACHE_USER_ID = "userId";
    private static final String CACHE_PLAY_LIST_COLLECTION = "playListCollection";
    private static final String CACHE_SUBLIST = "sublist";
    private static final String CACHE_PLAYLIST_TRACK_PREFIX = "playlist:";
    private static final String CACHE_ALBUM_TRACK_PREFIX = "album:";
    private static final String CACHE_QR_UNIKEY = "qrUnikey";
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setConnectionRequestTimeout(3000)
            .setSocketTimeout(10000)
            // 我们手动管理 cookie，关闭 HttpClient 自带的 cookie 解析，
            // 避免网易返回的 Expires 属性（含逗号）触发 "Invalid cookie header" 告警。
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build();

    private static final HashMap<String, Object> cache = new HashMap<String, Object>();

    public void login(){
        if (getCachedUserId() != null) {
            return;
        }

        // 使用配置中的 cookie 登录；无效则保持未登录，由界面引导扫码登录。
        String configuredCookie = normalizeConfiguredValue(NcmConfig.cookie);
        if (configuredCookie != null) {
            cache.put(CACHE_COOKIE, configuredCookie);
            ApiResult<LoginStatus> status = get(LoginStatus.class, "/login/status", null);
            LoginInfo.AccountBean account = getValidStatusAccount(status.body);
            if (account != null) {
                setAuthState(account.getId(), firstNonBlank(status.cookie, configuredCookie));
                return;
            }
            cache.remove(CACHE_COOKIE);
        }

        clearUserState();
    }

    @Override
    public void updateLoginState(){
        if (isBlank((String) cache.get(CACHE_COOKIE))) {
            clearUserState();
            login();
            return;
        }

        ApiResult<LoginInfo> refresh = get(LoginInfo.class, "/login/refresh", null);
        if (refresh.body == null || refresh.body.getCode() != 200) {
            clearAuthState();
            return;
        }
        if (!isBlank(refresh.cookie)) {
            cache.put(CACHE_COOKIE, refresh.cookie);
        }

        ApiResult<LoginStatus> status = get(LoginStatus.class, "/login/status", null);
        LoginInfo.AccountBean account = getValidStatusAccount(status.body);
        if (account == null) {
            clearAuthState();
            return;
        }
        setAuthState(account.getId(), firstNonBlank(status.cookie, (String) cache.get(CACHE_COOKIE)));
    }

    @Override
    public boolean isLoggedIn() {
        return getCachedUserId() != null;
    }

    @Override
    public String createQrCode() {
        ApiResult<QrKey> keyResult = get(QrKey.class, "/login/qr/key", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        if (keyResult.body == null || keyResult.body.getData() == null || isBlank(keyResult.body.getData().getUnikey())) {
            return null;
        }
        String unikey = keyResult.body.getData().getUnikey();

        ApiResult<QrCreate> createResult = get(QrCreate.class, "/login/qr/create", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("key", unikey),
                new BasicNameValuePair("qrimg", "true"),
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        if (createResult.body == null || createResult.body.getData() == null || isBlank(createResult.body.getData().getQrimg())) {
            return null;
        }

        cache.put(CACHE_QR_UNIKEY, unikey);
        return createResult.body.getData().getQrimg();
    }

    @Override
    public QrStatus checkQrStatus() {
        String unikey = (String) cache.get(CACHE_QR_UNIKEY);
        if (isBlank(unikey)) {
            return QrStatus.ERROR;
        }

        ApiResult<QrCheck> result = get(QrCheck.class, "/login/qr/check", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("key", unikey),
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        if (result.body == null) {
            return QrStatus.ERROR;
        }

        switch (result.body.getCode()) {
            case 801:
                return QrStatus.WAITING;
            case 802:
                return QrStatus.SCANNED;
            case 800:
                cache.remove(CACHE_QR_UNIKEY);
                return QrStatus.EXPIRED;
            case 803:
                cache.remove(CACHE_QR_UNIKEY);
                return finishQrLogin(firstNonBlank(result.body.getCookie(), result.cookie));
            default:
                return QrStatus.ERROR;
        }
    }

    /**
     * 扫码授权成功后，用返回的 cookie 拉取登录态、完成登录并持久化 cookie。
     */
    private QrStatus finishQrLogin(String rawCookie) {
        String cookie = sanitizeCookie(rawCookie);
        if (isBlank(cookie)) {
            return QrStatus.ERROR;
        }
        cache.put(CACHE_COOKIE, cookie);

        ApiResult<LoginStatus> status = get(LoginStatus.class, "/login/status", null);
        LoginInfo.AccountBean account = getValidStatusAccount(status.body);
        if (account == null) {
            cache.remove(CACHE_COOKIE);
            return QrStatus.ERROR;
        }

        String finalCookie = firstNonBlank(status.cookie, cookie);
        setAuthState(account.getId(), finalCookie);
        persistCookie(finalCookie);
        return QrStatus.CONFIRMED;
    }

    /**
     * 将 cookie 写回配置并保存，使下次进入游戏免扫码。best-effort，失败不影响本次登录。
     */
    private static void persistCookie(String cookie) {
        if (isBlank(cookie)) {
            return;
        }
        try {
            NcmConfig.cookie = cookie;
            net.minecraftforge.common.config.ConfigManager.sync(NcmMod.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
        } catch (Throwable t) {
            logger.warn("Failed to persist cookie to config: " + t.getMessage());
        }
    }

    @Override
    public ArrayList<PlayListContainer> LoadPlayList() {
        ArrayList<PlayListContainer> playlists = new ArrayList<PlayListContainer>();
        Long userId = getCachedUserId();
        if (userId == null) {
            return playlists;
        }

        PlayListCollection playListCollection;
        if (cache.containsKey(CACHE_PLAY_LIST_COLLECTION)) {
            playListCollection = (PlayListCollection) cache.get(CACHE_PLAY_LIST_COLLECTION);
        } else {
            playListCollection = getPlayListByUid(userId);
            if (isSuccessful(playListCollection)) {
                cache.put(CACHE_PLAY_LIST_COLLECTION, playListCollection);
            }
        }

        if (!isSuccessful(playListCollection) || playListCollection.getPlaylist() == null) {
            return playlists;
        }

        for (PlayList temp: playListCollection.getPlaylist()) {
            if (temp != null) {
                playlists.add(new PlayListContainer(temp.getId(), safeString(temp.getName())));
            }
        }
        return playlists;
    }

    @Override
    public ArrayList<TrackContainer> LoadTrackList(long id) {
        ArrayList<TrackContainer> tracks = new ArrayList<TrackContainer>();
        if (getCachedUserId() == null) {
            return tracks;
        }

        TrackCollection trackCollection = getTracksByPlaylistId(id);
        if (!isSuccessful(trackCollection) || trackCollection.getPlaylist() == null || trackCollection.getPlaylist().getTracks() == null) {
            return tracks;
        }

        for (PlayList.Tracks temp: trackCollection.getPlaylist().getTracks()) {
            if (temp != null) {
                tracks.add(new TrackContainer(
                        temp.getId(),
                        safeString(temp.getName()),
                        getPlaylistTrackArtist(temp),
                        getPlaylistTrackAlbum(temp),
                        temp.getDt()
                ));
            }
        }
        return tracks;
    }

    @Override
    public ArrayList<TrackContainer> LoadAlbumTrackList(long id) {
        ArrayList<TrackContainer> tracks = new ArrayList<TrackContainer>();
        if (getCachedUserId() == null) {
            return tracks;
        }

        AlbumTracks albumTracks = getTracksByAlbumId(id);
        if (albumTracks == null || albumTracks.getCode() != 200 || albumTracks.getSongs() == null) {
            return tracks;
        }

        for (AlbumTracks.SongsBean temp: albumTracks.getSongs()) {
            if (temp != null) {
                tracks.add(new TrackContainer(
                        temp.getId(),
                        safeString(temp.getName()),
                        getAlbumTrackArtist(temp),
                        getAlbumTrackAlbum(temp),
                        temp.getDt()
                ));
            }
        }
        return tracks;
    }

    @Override
    public ArrayList<SubListContainer> LoadSubList(){
        ArrayList<SubListContainer> albums = new ArrayList<SubListContainer>();
        if (getCachedUserId() == null) {
            return albums;
        }

        Sublist sublist;
        if (cache.containsKey(CACHE_SUBLIST)) {
            sublist = (Sublist) cache.get(CACHE_SUBLIST);
        } else {
            sublist = getSublist();
            if (sublist != null && sublist.getCode() == 200) {
                cache.put(CACHE_SUBLIST, sublist);
            }
        }

        if (sublist == null || sublist.getCode() != 200 || sublist.getData() == null) {
            return albums;
        }

        for (Sublist.DataBean temp: sublist.getData()) {
            if (temp != null) {
                albums.add(new SubListContainer(temp.getId(), safeString(temp.getName())));
            }
        }
        return albums;
    }

    /**
     * 获取歌曲 url
     * @Param: [id]
     * @Return: java.lang.String
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:42
     */
    @Override
    public ArrayList<TrackContainer> SearchSongs(String keywords) {
        if (getCachedUserId() == null || isBlank(keywords)) {
            return new ArrayList<TrackContainer>();
        }

        ApiResult<SearchSongs> result = get(SearchSongs.class, "/search", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("keywords", keywords.trim()),
                new BasicNameValuePair("type", "1"),
                new BasicNameValuePair("limit", "50"),
                new BasicNameValuePair("offset", "0"),
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        return toSearchTrackContainers(result.body);
    }

    static ArrayList<TrackContainer> toSearchTrackContainers(SearchSongs response) {
        ArrayList<TrackContainer> tracks = new ArrayList<TrackContainer>();
        if (response == null || response.getCode() != 200 || response.getResult() == null || response.getResult().getSongs() == null) {
            return tracks;
        }

        for (SearchSongs.Song song : response.getResult().getSongs()) {
            if (song != null) {
                tracks.add(new TrackContainer(
                        song.getId(),
                        safeString(song.getName()),
                        getSearchTrackArtist(song),
                        getSearchTrackAlbum(song),
                        song.getDt()
                ));
            }
        }
        return tracks;
    }

    @Override
    public String GetMusicById(long id) {
        ApiResult<MusicPacket> result = get(MusicPacket.class, "/song/url/v1", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("id", Long.toString(id)),
                new BasicNameValuePair("level", getSongLevel()),
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        MusicPacket packet = result.body;
        if (packet == null || packet.getCode() != 200 || packet.getData() == null || packet.getData().isEmpty() || packet.getData().get(0) == null) {
            return "";
        }

        MusicPacket.Data data = packet.getData().get(0);
        if (!isBlank(data.getProxyUrl())) {
            return data.getProxyUrl();
        }
        return safeString(data.getUrl());
    }

    /**
     * 私人FM
     * @Return: team.info.ncmfm.entity.PersonalFM
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:39
     */
    public PersonalFM personalFm(){
        if (getCachedUserId() == null) {
            return null;
        }
        ApiResult<PersonalFM> result = get(PersonalFM.class, "/personal_fm", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        if (result.body == null || result.body.getCode() != 200) {
            return null;
        }
        return result.body;
    }

    private PlayListCollection getPlayListByUid(long uid){
        ApiResult<PlayListCollection> result = get(PlayListCollection.class, "/user/playlist", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("uid", Long.toString(uid)),
                new BasicNameValuePair("limit", "1000"),
                new BasicNameValuePair("offset", "0")
        ));
        if (!isSuccessful(result.body)) {
            return null;
        }
        return result.body;
    }

    private TrackCollection getTracksByPlaylistId(long id){
        String cacheKey = CACHE_PLAYLIST_TRACK_PREFIX + id;
        if (cache.containsKey(cacheKey)) {
            return (TrackCollection) cache.get(cacheKey);
        }

        ApiResult<TrackCollection> result = get(TrackCollection.class, "/playlist/detail", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("id", Long.toString(id))
        ));
        if (isSuccessful(result.body)) {
            cache.put(cacheKey, result.body);
            return result.body;
        }
        return null;
    }

    private AlbumTracks getTracksByAlbumId(long id){
        String cacheKey = CACHE_ALBUM_TRACK_PREFIX + id;
        if (cache.containsKey(cacheKey)) {
            return (AlbumTracks) cache.get(cacheKey);
        }

        ApiResult<AlbumTracks> result = get(AlbumTracks.class, "/album", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("id", Long.toString(id))
        ));
        if (result.body != null && result.body.getCode() == 200) {
            cache.put(cacheKey, result.body);
            return result.body;
        }
        return null;
    }

    /**
     * 获取收藏专辑列表
     * @Return: team.info.ncmfm.entity.Sublist
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:40
     */
    private Sublist getSublist(){
        ApiResult<Sublist> result = get(Sublist.class, "/album/sublist", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("limit", "1000"),
                new BasicNameValuePair("offset", "0")
        ));
        if (result.body == null || result.body.getCode() != 200) {
            return null;
        }
        return result.body;
    }

    private static <T> ApiResult<T> get(Class<T> responseType, String path, List<NameValuePair> params) {
        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(REQUEST_CONFIG).build()) {
            URI uri = buildUri(path, params);
            HttpGet request = new HttpGet(uri);
            addCommonHeaders(request);
            try (CloseableHttpResponse response = client.execute(request)) {
                return parseResponse(responseType, path, response);
            }
        } catch (Exception e) {
            logger.error("Netease api request failed for " + path + ": " + e.getMessage());
            return new ApiResult<T>(null, null);
        }
    }

    private static URI buildUri(String path, List<NameValuePair> params) throws Exception {
        String host = NcmConfig.host == null ? "" : NcmConfig.host.trim();
        if (host.length() == 0) {
            throw new IllegalArgumentException("empty host");
        }

        URIBuilder builder = new URIBuilder(host);
        String basePath = builder.getPath();
        if (basePath == null || basePath.length() == 0 || "/".equals(basePath)) {
            builder.setPath(path);
        } else {
            builder.setPath(trimTrailingSlash(basePath) + path);
        }
        if (params != null) {
            builder.addParameters(params);
        }
        return builder.build();
    }

    private static void addCommonHeaders(HttpGet request) {
        request.addHeader("User-Agent", USER_AGENT);
        String cookie = (String) cache.get(CACHE_COOKIE);
        if (!isBlank(cookie)) {
            request.addHeader("Cookie", cookie);
        }
    }

    private static <T> ApiResult<T> parseResponse(Class<T> responseType, String path, CloseableHttpResponse response) throws Exception {
        String cookie = extractCookieHeader(response);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            logger.error("Netease api request failed for " + path + ": HTTP " + statusCode);
            EntityUtils.consumeQuietly(response.getEntity());
            return new ApiResult<T>(null, null);
        }

        String jsonResult = EntityUtils.toString(response.getEntity(), "UTF-8");
        T body = new Gson().fromJson(jsonResult, responseType);
        String bodyCookie = getBodyCookie(body);
        if (!isBlank(bodyCookie)) {
            cookie = bodyCookie;
        }
        return new ApiResult<T>(body, cookie);
    }

    private static String extractCookieHeader(CloseableHttpResponse response) {
        Header[] headers = response.getHeaders("Set-Cookie");
        StringBuilder cookie = new StringBuilder();
        for (Header header : headers) {
            if (header == null || isBlank(header.getValue())) {
                continue;
            }
            String value = header.getValue();
            int separator = value.indexOf(';');
            String pair = separator >= 0 ? value.substring(0, separator) : value;
            pair = pair.trim();
            if (pair.length() == 0) {
                continue;
            }
            if (cookie.length() > 0) {
                cookie.append("; ");
            }
            cookie.append(pair);
        }
        return cookie.length() == 0 ? null : cookie.toString();
    }

    private static String getBodyCookie(Object body) {
        if (body instanceof LoginInfo) {
            return ((LoginInfo) body).getCookie();
        }
        return null;
    }

    private static LoginInfo.AccountBean getValidStatusAccount(LoginStatus status) {
        if (status == null || status.getData() == null || status.getData().getCode() != 200 || status.getData().getAccount() == null) {
            return null;
        }
        return status.getData().getAccount();
    }

    private static void setAuthState(long userId, String cookie) {
        Long cachedUserId = getCachedUserId();
        if (cachedUserId == null || cachedUserId.longValue() != userId) {
            clearContentCaches();
        }

        cache.put(CACHE_USER_ID, Long.valueOf(userId));
        if (!isBlank(cookie)) {
            cache.put(CACHE_COOKIE, cookie);
        }
    }

    private static void clearAuthState() {
        cache.remove(CACHE_COOKIE);
        clearUserState();
    }

    private static void clearUserState() {
        cache.remove(CACHE_USER_ID);
        clearContentCaches();
    }

    private static void clearContentCaches() {
        cache.remove(CACHE_PLAY_LIST_COLLECTION);
        cache.remove(CACHE_SUBLIST);
        Iterator<String> iterator = cache.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.startsWith(CACHE_PLAYLIST_TRACK_PREFIX) || key.startsWith(CACHE_ALBUM_TRACK_PREFIX)) {
                iterator.remove();
            }
        }
    }

    private static Long getCachedUserId() {
        Object userId = cache.get(CACHE_USER_ID);
        if (userId instanceof Number) {
            return Long.valueOf(((Number) userId).longValue());
        }
        return null;
    }

    private static boolean isSuccessful(PlayListCollection collection) {
        return collection != null && collection.getCode() == 200;
    }

    private static boolean isSuccessful(TrackCollection collection) {
        return collection != null && collection.getCode() == 200;
    }

    private static String getPlaylistTrackArtist(PlayList.Tracks track) {
        if (track.getAr() == null || track.getAr().isEmpty() || track.getAr().get(0) == null) {
            return "";
        }
        return safeString(track.getAr().get(0).getName());
    }

    private static String getPlaylistTrackAlbum(PlayList.Tracks track) {
        if (track.getAl() == null) {
            return "";
        }
        return safeString(track.getAl().getName());
    }

    private static String getAlbumTrackArtist(AlbumTracks.SongsBean track) {
        if (track.getAr() == null || track.getAr().isEmpty() || track.getAr().get(0) == null) {
            return "";
        }
        return safeString(track.getAr().get(0).getName());
    }

    private static String getAlbumTrackAlbum(AlbumTracks.SongsBean track) {
        if (track.getAl() == null) {
            return "";
        }
        return safeString(track.getAl().getName());
    }

    private static String getSearchTrackArtist(SearchSongs.Song track) {
        if (track.getArtists() == null || track.getArtists().isEmpty() || track.getArtists().get(0) == null) {
            return "";
        }
        return safeString(track.getArtists().get(0).getName());
    }

    private static String getSearchTrackAlbum(SearchSongs.Song track) {
        if (track.getAlbum() == null) {
            return "";
        }
        return safeString(track.getAlbum().getName());
    }

    private static String getSongLevel() {
        try {
            if (Long.parseLong(safeString(NcmConfig.bitRate).trim()) >= 320000L) {
                return "exhigh";
            }
        } catch (NumberFormatException ignored) {
            return "standard";
        }
        return "standard";
    }

    private static String normalizeConfiguredValue(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private static String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    /**
     * 清洗扫码接口 body 中返回的原始 cookie 串：可能是多个 Set-Cookie 拼接而成，
     * 包含 Max-Age/Expires/Path 等属性段。这里只保留真正的 name=value（如 MUSIC_U、__csrf），
     * 拼成可直接用于请求头 Cookie 的字符串。
     */
    private static String sanitizeCookie(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String segment : raw.split(";")) {
            String pair = segment.trim();
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String name = pair.substring(0, eq).trim();
            if (isCookieAttribute(name)) {
                continue;
            }
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append(pair);
        }
        return result.length() == 0 ? null : result.toString();
    }

    private static boolean isCookieAttribute(String name) {
        return "Max-Age".equalsIgnoreCase(name)
                || "Expires".equalsIgnoreCase(name)
                || "Path".equalsIgnoreCase(name)
                || "Domain".equalsIgnoreCase(name)
                || "SameSite".equalsIgnoreCase(name)
                || "Secure".equalsIgnoreCase(name)
                || "HttpOnly".equalsIgnoreCase(name)
                || "HTTPOnly".equalsIgnoreCase(name);
    }

    private static String trimTrailingSlash(String value) {
        int end = value.length();
        while (end > 1 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    @Override
    public String getLyricById(long id) {
        ApiResult<team.info.ncmfm.entity.LyricResponse> result = get(team.info.ncmfm.entity.LyricResponse.class, "/lyric", Arrays.<NameValuePair>asList(
                new BasicNameValuePair("id", Long.toString(id)),
                new BasicNameValuePair("timestamp", Long.toString(System.currentTimeMillis()))
        ));
        if (result.body == null || result.body.getCode() != 200 || result.body.getLrc() == null) {
            return null;
        }
        return result.body.getLrc().getLyric();
    }

    private static class ApiResult<T> {
        private final T body;
        private final String cookie;

        private ApiResult(T body, String cookie) {
            this.body = body;
            this.cookie = cookie;
        }
    }
}
