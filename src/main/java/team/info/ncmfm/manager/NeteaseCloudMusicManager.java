package team.info.ncmfm.manager;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.NcmConfig;
import team.info.ncmfm.entity.*;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeteaseCloudMusicManager implements IMusicManager {
    private static final Logger logger = LogManager.getLogger(NeteaseCloudMusicManager.class);
    private static final String HOST=NcmConfig.host;
    public static HashMap<String, Object> cache=new HashMap<String,Object>();

    private String bitRate=NcmConfig.bitRate;


    public void login(){
        try{
            String phone=NcmConfig.phone;
            String password= NcmConfig.password;
            String url=HOST+"/login/cellphone?phone="+phone+"&password="+password;
            if(cache.isEmpty()){
                LoginInfo rs=doGet(LoginInfo.class,url);
                if(rs.getCode()==200){
                    cache.put("loginInfo",rs);

                    String regex="MUSIC_U=([^;]*)(|$)";
                    String input=rs.getCookie();
                    Pattern r = Pattern.compile(regex,Pattern.MULTILINE);
                    Matcher m = r.matcher(input);

                    if(m.find()) {
                        cache.put("Cookie",m.group(0));
                    }
                }
            }
        }catch (Exception ex){
            logger.error("云音乐登录失败："+ex.getMessage());
        }
    }

    @Override
    public void updateLoginState(){
        String url=HOST+"/login/refresh";
        try {
           doGet(url);
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public ArrayList<PlayListContainer> LoadPlayList() {
        ArrayList<PlayListContainer> as=new ArrayList<>();

        if(cache.containsKey("loginInfo")){
            LoginInfo loginInfo=(LoginInfo)cache.get("loginInfo");
            PlayListCollection playListCollection=null;

            if(!cache.containsKey("playListCollection")){
                playListCollection=GetPlayListByUid(loginInfo.getAccount().getId());
                cache.put("playListCollection",playListCollection);
            }else {
                playListCollection=(PlayListCollection)cache.get("playListCollection");
            }

            if(playListCollection!=null){
                for(PlayList temp: playListCollection.getPlaylist()){
                    as.add(new PlayListContainer(temp.getId(),temp.getName()));
                }
            }
        }
        return as;
    }

    @Override
    public ArrayList<TrackContainer> LoadTrackList(long id) {
        ArrayList<TrackContainer> as=new ArrayList<>();
        TrackCollection trackCollection=null;

        String collectionId=Long.toString(id);
        if(cache.containsKey(collectionId)){
            trackCollection=(TrackCollection)cache.get(collectionId);
        }else {
            trackCollection=GetTracksById(id);
            cache.put(collectionId,trackCollection);
        }
        if(trackCollection!=null){
            for(PlayList.Tracks temp: trackCollection.getPlaylist().getTracks()){
                as.add(new TrackContainer(
                        temp.getId(),
                        temp.getName(),
                        temp.getAr().get(0).getName(),
                        temp.getAl().getName()
                ));
            }
        }
        return as;
    }

    @Override
    public ArrayList<SubListContainer> LoadSubList(){
        ArrayList<SubListContainer> as=new ArrayList<>();

        if(cache.containsKey("loginInfo")){

            LoginInfo loginInfo=(LoginInfo)cache.get("loginInfo");
            Sublist sublist=null;

            if(!cache.containsKey("sublist")){
                sublist=GetSublist();
                cache.put("sublist",sublist);
            }else {
                sublist=(Sublist)cache.get("sublist");
            }

            if(sublist!=null){
                for(Sublist.DataBean temp: sublist.getData()){
                    as.add(new SubListContainer(temp.getId(),temp.getName()));
                }
            }
        }
        return as;
    }

    /** 
     * 获取音乐 url
     * @Param: [id]
     * @Return: java.lang.String
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:42
     */
    @Override
    public String GetMusicById(long id) {
        String url=HOST+"/song/url?id="+id+"&br="+ bitRate;
        String musicUrl="";
        MusicPacket packet=doGet(MusicPacket.class,url);
        if(packet.getCode()==200){
            musicUrl=packet.getData().get(0).getUrl();
        }
        return musicUrl;
    }

    private PlayListCollection GetPlayListByUid(int uid){
        String url=HOST+"/user/playlist?uid="+uid;
        return doGet(PlayListCollection.class,url);
    }

    public static TrackCollection GetTracksById(long id){
        String url=HOST+"/playlist/detail?id="+id;
        return doGet(TrackCollection.class,url);
    }

    public static AlbumTracks GetTracksBySubId(long id){
        String url=HOST+"/album?id="+id;
        return doGet(AlbumTracks.class,url);
    }

    /**
     * 已收藏专辑列表
     * @Return: team.info.ncmfm.entity.Sublist
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:40
     */
    private Sublist GetSublist(){
        String url=HOST+"/album/sublist";
        return doGet(Sublist.class,url);
    }

    /**
     * 私人FM
     * @Return: team.info.ncmfm.entity.PersonalFM
     * @Author: FOXCELL
     * @Date: 2020/11/23 9:39
     */
    public PersonalFM personalFm(){
        long timestamp=new Date().getTime();
        String url=HOST+"/personal_fm?timestamp="+timestamp;
        return doGet(PersonalFM.class,url);
    }

    private HttpResponse doGet(String url) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        if(cache.size()>0){
            get.addHeader("Cookie",(String)cache.get("Cookie"));
        }
        return client.execute(get);
    }

    private static <T> T doGet(Class <T> t,String url){
        T rs=null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(200)
                .setConnectionRequestTimeout(200)
                // 请求超时400ms
                .setSocketTimeout(5000)
                .build();

        HttpGet get = new HttpGet(url);
        get.setConfig(requestConfig);

        try {
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            if(cache.size()>0){
                get.addHeader("Cookie",(String)cache.get("Cookie"));
            }
            HttpResponse response = client.execute(get);
            String jsonResult = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            rs=gson.fromJson(jsonResult,(Type)t);
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
        return rs;
    }
}
