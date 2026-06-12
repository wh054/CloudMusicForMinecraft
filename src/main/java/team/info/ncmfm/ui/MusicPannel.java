package team.info.ncmfm.ui;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import team.info.ncmfm.component.GuiSlotPlayList;
import team.info.ncmfm.component.GuiSlotSubList;
import team.info.ncmfm.component.GuiSlotTracks;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.manager.MusicPlaybackManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MusicPannel extends GuiScreen {

    private static final int BUTTON_STOP_MUSIC = 0;
    private static final int BUTTON_REFLASH_STATE = 1;
    private static final int BUTTON_PERSONAL_FM = 2;
    private static final int BUTTON_NAV_PLAYLISTS = 10;
    private static final int BUTTON_NAV_ALBUMS = 11;
    private static final int BUTTON_NAV_SEARCH = 12;
    private static final int BUTTON_NAV_QUEUE = 13;
    private static final int BUTTON_ADD_QUEUE = 14;
    private static final int BUTTON_PREVIOUS = 15;
    private static final int BUTTON_PLAY_SELECTED = 16;
    private static final int BUTTON_NEXT = 17;
    private static final int BUTTON_CLOSE = 18;
    private static final int BUTTON_SEARCH = 19;
    private static final int BUTTON_TOGGLE_MODE = 20;
    private static final int BUTTON_VOLUME_TOGGLE = 21;
    private static final int BUTTON_TOGGLE_LYRICS = 22;
    private static final int BUTTON_TOGGLE_QUALITY = 23;

    private static final int VIEW_PLAYLISTS = 0;
    private static final int VIEW_ALBUMS = 1;
    private static final int VIEW_TRACKS = 2;
    private static final int VIEW_SEARCH = 3;
    private static final int VIEW_QUEUE = 4;

    private final ArrayList<PlayListContainer> playList;
    private final ArrayList<TrackContainer> trackList;
    private final ArrayList<TrackContainer> searchResults;
    private final ArrayList<SubListContainer> subList;
    private final ArrayList<TrackContainer> queue;

    private GuiSlotPlayList slotPlayList;
    private GuiSlotSubList slotSubList;
    private GuiSlotTracks slotTracks;
    private GuiTextField searchField;

    private int playList_selected_index = -1;
    private int subList_selected_index = -1;
    private int track_selected_index = -1;
    private int activeView = VIEW_PLAYLISTS;

    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int contentLeft;
    private int contentTop;
    private int contentWidth;
    private int contentHeight;
    private int sidebarWidth;
    private int headerHeight;
    private int playerHeight;

    private String viewTitle = "My Playlists";
    private String statusMessage = "";
    private String activeSourceLabel = "Library";

    private PlayListContainer selectedPlayList;
    private SubListContainer selectedSubList;
    private TrackContainer selectedTrack;

    private final IMusicManager musicManager;
    private BlockPos blockPos;

    public MusicPannel(Minecraft mc, IMusicManager musicManager) {
        playList = new ArrayList<PlayListContainer>();
        trackList = new ArrayList<TrackContainer>();
        searchResults = new ArrayList<TrackContainer>();
        subList = new ArrayList<SubListContainer>();
        this.queue = (ArrayList<TrackContainer>) MusicPlaybackManager.getInstance().getQueue();
        this.musicManager = musicManager;
    }

    public MusicPannel(Minecraft mc, IMusicManager musicManager, BlockPos pos) {
        playList = new ArrayList<PlayListContainer>();
        trackList = new ArrayList<TrackContainer>();
        searchResults = new ArrayList<TrackContainer>();
        subList = new ArrayList<SubListContainer>();
        this.queue = (ArrayList<TrackContainer>) MusicPlaybackManager.getInstance().getQueue();
        this.musicManager = musicManager;
        this.blockPos = pos;
    }

    @Override
    public void initGui() {
        musicManager.login();
        if (!musicManager.isLoggedIn()) {
            mc.displayGuiScreen(new QrLoginScreen(mc, musicManager, blockPos));
            return;
        }

        calculateLayout();
        reloadLibrary();
        createControls();
        createSlots();
        switchView(activeView);
    }

    private void calculateLayout() {
        panelWidth = Math.max(340, Math.min(width - 24, 460));
        panelHeight = Math.max(240, Math.min(height - 20, 300));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        sidebarWidth = 94;
        headerHeight = 30;
        playerHeight = 62;
        contentLeft = panelLeft + sidebarWidth + 8;
        contentTop = panelTop + headerHeight + 8;
        contentWidth = panelWidth - sidebarWidth - 18;
        contentHeight = panelHeight - headerHeight - playerHeight - 18;
    }

    private void createControls() {
        buttonList.clear();

        int navLeft = panelLeft + 8;
        int navTop = panelTop + headerHeight + 8;
        int navWidth = sidebarWidth - 16;
        buttonList.add(new GuiModernButton(BUTTON_NAV_PLAYLISTS, navLeft, navTop, navWidth, 18, "歌单列表"));
        buttonList.add(new GuiModernButton(BUTTON_NAV_ALBUMS, navLeft, navTop + 22, navWidth, 18, "收藏专辑"));
        buttonList.add(new GuiModernButton(BUTTON_NAV_SEARCH, navLeft, navTop + 44, navWidth, 18, "音乐搜索"));
        buttonList.add(new GuiModernButton(BUTTON_PERSONAL_FM, navLeft, navTop + 66, navWidth, 18, "私人 FM"));
        buttonList.add(new GuiModernButton(BUTTON_NAV_QUEUE, navLeft, navTop + 88, navWidth, 18, "播放队列"));

        buttonList.add(new GuiModernButton(BUTTON_REFLASH_STATE, panelLeft + panelWidth - 112, panelTop + 6, 52, 18, "刷新"));
        buttonList.add(new GuiModernButton(BUTTON_CLOSE, panelLeft + panelWidth - 56, panelTop + 6, 46, 18, "关闭"));

        buttonList.add(new GuiModernButton(BUTTON_SEARCH, contentLeft + contentWidth - 58, contentTop, 58, 18, "搜索"));
        buttonList.add(new GuiModernButton(BUTTON_ADD_QUEUE, contentLeft + contentWidth - 58, contentTop, 58, 18, "+队列"));

        int playerTop = panelTop + panelHeight - playerHeight;
        int controlsRight = panelLeft + panelWidth - 10;

        // Row 1 of Player Controls (146px width total)
        buttonList.add(new GuiModernButton(BUTTON_PREVIOUS, controlsRight - 146, playerTop + 12, 32, 18, "上首"));
        buttonList.add(new GuiModernButton(BUTTON_PLAY_SELECTED, controlsRight - 112, playerTop + 12, 34, 18, "播放"));
        buttonList.add(new GuiModernButton(BUTTON_NEXT, controlsRight - 76, playerTop + 12, 32, 18, "下首"));
        buttonList.add(new GuiModernButton(BUTTON_STOP_MUSIC, controlsRight - 42, playerTop + 12, 42, 18, "停止"));

        // Row 2 of Player Controls (aligned perfectly with Row 1)
        String modeText = getModeShortText(MusicPlaybackManager.getInstance().getPlayMode());
        String volText = "V:" + Math.round(MusicPlaybackManager.getInstance().getVolume() * 100);
        String lyricText = team.info.ncmfm.NcmConfig.showLyrics ? "词开" : "词关";
        String qualityText = getQualityShortText(team.info.ncmfm.NcmConfig.audioQuality);
        buttonList.add(new GuiModernButton(BUTTON_TOGGLE_MODE, controlsRight - 146, playerTop + 34, 32, 18, modeText));
        buttonList.add(new GuiModernButton(BUTTON_VOLUME_TOGGLE, controlsRight - 112, playerTop + 34, 34, 18, volText));
        buttonList.add(new GuiModernButton(BUTTON_TOGGLE_LYRICS, controlsRight - 76, playerTop + 34, 32, 18, lyricText));
        buttonList.add(new GuiModernButton(BUTTON_TOGGLE_QUALITY, controlsRight - 42, playerTop + 34, 42, 18, qualityText));

        searchField = new GuiTextField(0, fontRenderer, contentLeft, contentTop, Math.max(80, contentWidth - 64), 18);
        searchField.setMaxStringLength(64);
    }

    private void createSlots() {
        int listTop = contentTop + 18;
        int listHeight = Math.max(36, contentHeight - 18);
        int trackTop = contentTop + 30;
        int trackHeight = Math.max(36, contentHeight - 30);
        slotPlayList = new GuiSlotPlayList(this, playList, contentLeft, listTop, contentWidth, listHeight, 20);
        slotSubList = new GuiSlotSubList(this, subList, contentLeft, listTop, contentWidth, listHeight, 20);
        slotTracks = new GuiSlotTracks(this, trackList, contentLeft, trackTop, contentWidth, trackHeight, 24);
    }

    private void reloadLibrary() {
        playList.clear();
        subList.clear();
        playList.addAll(musicManager.LoadPlayList());
        subList.addAll(musicManager.LoadSubList());
        playList_selected_index = -1;
        subList_selected_index = -1;
        selectedPlayList = null;
        selectedSubList = null;
        statusMessage = MusicPlaybackManager.getInstance().getStatusMessage();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateButtonVisibility();
        drawDefaultBackground();
        drawPanel();
        drawActiveList(mouseX, mouseY, partialTicks);
        drawContentStatus();
        drawPlayerBar();
        if (activeView == VIEW_SEARCH && searchField != null) {
            searchField.drawTextBox();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPanel() {
        // Main panel background (sleek dark mode)
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xEE11161B);
        // Header bar
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + headerHeight, 0xFF1C2229);
        // Sidebar background
        drawRect(panelLeft, panelTop + headerHeight, panelLeft + sidebarWidth, panelTop + panelHeight - playerHeight, 0xEE151A20);
        // Player bottom bar background
        drawRect(panelLeft, panelTop + panelHeight - playerHeight, panelLeft + panelWidth, panelTop + panelHeight, 0xFF1C2229);
        // Main list container background border
        drawRect(contentLeft - 4, contentTop - 4, contentLeft + contentWidth + 4, contentTop + contentHeight + 4, 0x22FFFFFF);

        drawString(fontRenderer, "网易云音乐", panelLeft + 10, panelTop + 10, 0xFF5555);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(viewTitle, contentWidth - 118), contentLeft, panelTop + 10, 0xEAEAEA);
        drawString(fontRenderer, blockPos == null ? "立体声播放" : "3D位置播放", panelLeft + 10, panelTop + panelHeight - 14, 0x777777);
        drawSelectedNavigation();
    }

    private void drawSelectedNavigation() {
        int selectedY = -1;
        if (activeView == VIEW_PLAYLISTS) {
            selectedY = panelTop + headerHeight + 8;
        } else if (activeView == VIEW_ALBUMS) {
            selectedY = panelTop + headerHeight + 30;
        } else if (activeView == VIEW_SEARCH) {
            selectedY = panelTop + headerHeight + 52;
        } else if (activeView == VIEW_QUEUE) {
            selectedY = panelTop + headerHeight + 96;
        }
        if (selectedY >= 0) {
            // Sleek left-side border indicator for active navigation tab
            drawRect(panelLeft, selectedY - 2, panelLeft + 3, selectedY + 20, 0xFFE54343);
        }
    }

    private void drawActiveList(int mouseX, int mouseY, float partialTicks) {
        if (activeView == VIEW_PLAYLISTS && slotPlayList != null) {
            slotPlayList.drawScreen(mouseX, mouseY, partialTicks);
        } else if (activeView == VIEW_ALBUMS && slotSubList != null) {
            slotSubList.drawScreen(mouseX, mouseY, partialTicks);
        } else if ((activeView == VIEW_TRACKS || activeView == VIEW_SEARCH || activeView == VIEW_QUEUE) && slotTracks != null) {
            slotTracks.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    private void drawContentStatus() {
        String message = MusicPlaybackManager.getInstance().getStatusMessage();
        if (activeView == VIEW_PLAYLISTS && playList.isEmpty()) {
            message = "无可用歌单";
        } else if (activeView == VIEW_ALBUMS && subList.isEmpty()) {
            message = "无收藏专辑";
        } else if ((activeView == VIEW_TRACKS || activeView == VIEW_SEARCH || activeView == VIEW_QUEUE) && trackList.isEmpty()) {
            message = message.trim().length() == 0 ? "列表无歌曲" : message;
        }

        if (message != null && message.length() > 0) {
            int y = activeView == VIEW_SEARCH ? contentTop + 22 : contentTop;
            drawString(fontRenderer, fontRenderer.trimStringToWidth(message, contentWidth - 8), contentLeft, y, 0x888888);
        }
    }

    private void drawPlayerBar() {
        MusicPlaybackManager manager = MusicPlaybackManager.getInstance();
        int playerTop = panelTop + panelHeight - playerHeight;

        // Render progress bar
        drawProgressBar(panelLeft + 10, playerTop + 4, panelWidth - 20);

        TrackContainer track = manager.getCurrentTrack();
        String title = track == null ? "暂无播放曲目" : safeText(track.getName());
        String author = track == null ? "Ready" : safeText(track.getAuthor());

        // Get progress time
        long elapsedSec = manager.getProgressTicks() / 20;
        long totalSec = (track != null && track.getDurationMs() > 0) ? track.getDurationMs() / 1000 : -1;
        String timeStr = formatTime(elapsedSec) + " / " + formatTime(totalSec);

        String meta = author + "  [" + timeStr + "]";
        int textWidth = Math.max(80, panelWidth - 170);

        drawString(fontRenderer, fontRenderer.trimStringToWidth(title, textWidth), panelLeft + 10, playerTop + 12, 0xFFFFFF);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(meta, textWidth), panelLeft + 10, playerTop + 28, 0x888888);
    }

    private void drawProgressBar(int x, int y, int barWidth) {
        MusicPlaybackManager manager = MusicPlaybackManager.getInstance();
        TrackContainer track = manager.getCurrentTrack();

        long elapsedSec = manager.getProgressTicks() / 20;
        long totalSec = -1;
        float progress = 0.0f;

        if (track != null && track.getDurationMs() > 0) {
            totalSec = track.getDurationMs() / 1000;
            if (totalSec > 0) {
                progress = (float) elapsedSec / totalSec;
                progress = Math.max(0.0f, Math.min(1.0f, progress));
            }
        }

        // Draw progress background bar (Lighter grey for visibility)
        drawRect(x, y, x + barWidth, y + 3, 0xFF555555);

        // Draw filled progress bar (NetEase red)
        if (progress > 0.0f) {
            int fillWidth = (int) (barWidth * progress);
            drawRect(x, y, x + fillWidth, y + 3, 0xFFEF4444);
        }
    }

    private String formatTime(long seconds) {
        if (seconds < 0) return "--:--";
        long m = seconds / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void updateButtonVisibility() {
        for (GuiButton button : buttonList) {
            if (button.id == BUTTON_SEARCH) {
                button.visible = activeView == VIEW_SEARCH;
            } else if (button.id == BUTTON_ADD_QUEUE) {
                button.visible = activeView == VIEW_TRACKS || activeView == VIEW_SEARCH;
            } else if (button.id == BUTTON_PLAY_SELECTED) {
                button.visible = activeView == VIEW_TRACKS || activeView == VIEW_SEARCH || activeView == VIEW_QUEUE;
            }
        }
    }

    @Override
    public void updateScreen() {
        if (searchField != null) {
            searchField.updateCursorCounter();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        MusicPlaybackManager manager = MusicPlaybackManager.getInstance();
        switch (button.id) {
            case BUTTON_REFLASH_STATE:
                musicManager.updateLoginState();
                if (!musicManager.isLoggedIn()) {
                    mc.displayGuiScreen(new QrLoginScreen(mc, musicManager, blockPos));
                    return;
                }
                statusMessage = "Library refreshed";
                reloadLibrary();
                switchView(activeView);
                break;
            case BUTTON_STOP_MUSIC:
                manager.stop();
                break;
            case BUTTON_PERSONAL_FM:
                manager.playPersonalFm();
                switchView(VIEW_QUEUE); // Redirect to queue view to show active playback
                viewTitle = "私人 FM";
                break;
            case BUTTON_NAV_PLAYLISTS:
                switchView(VIEW_PLAYLISTS);
                break;
            case BUTTON_NAV_ALBUMS:
                switchView(VIEW_ALBUMS);
                break;
            case BUTTON_NAV_SEARCH:
                switchView(VIEW_SEARCH);
                break;
            case BUTTON_NAV_QUEUE:
                switchView(VIEW_QUEUE);
                break;
            case BUTTON_SEARCH:
                runSearch();
                break;
            case BUTTON_ADD_QUEUE:
                addSelectedToQueue();
                break;
            case BUTTON_PREVIOUS:
                manager.previous();
                break;
            case BUTTON_PLAY_SELECTED:
                PlayMusic();
                break;
            case BUTTON_NEXT:
                manager.next();
                break;
            case BUTTON_TOGGLE_MODE:
                manager.togglePlayMode();
                button.displayString = getModeShortText(manager.getPlayMode());
                break;
            case BUTTON_VOLUME_TOGGLE:
                float currentVol = manager.getVolume();
                float nextVol;
                if (currentVol < 0.1f) nextVol = 0.20f;
                else if (currentVol < 0.3f) nextVol = 0.40f;
                else if (currentVol < 0.5f) nextVol = 0.60f;
                else if (currentVol < 0.7f) nextVol = 0.80f;
                else if (currentVol < 0.9f) nextVol = 1.00f;
                else nextVol = 0.00f;
                manager.changeVolume(nextVol - currentVol);
                button.displayString = "V:" + Math.round(nextVol * 100);
                break;
            case BUTTON_TOGGLE_LYRICS:
                team.info.ncmfm.NcmConfig.showLyrics = !team.info.ncmfm.NcmConfig.showLyrics;
                try {
                    net.minecraftforge.common.config.ConfigManager.sync(team.info.ncmfm.NcmMod.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
                } catch (Exception e) {}
                button.displayString = team.info.ncmfm.NcmConfig.showLyrics ? "词开" : "词关";
                break;
            case BUTTON_TOGGLE_QUALITY:
                toggleQuality();
                button.displayString = getQualityShortText(team.info.ncmfm.NcmConfig.audioQuality);
                break;
            case BUTTON_CLOSE:
                mc.displayGuiScreen(null);
                break;
            default:
                super.actionPerformed(button);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (activeView == VIEW_SEARCH && (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)) {
            runSearch();
            return;
        }
        if (activeView == VIEW_SEARCH && searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (activeView == VIEW_SEARCH && searchField != null) {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public Minecraft getMinecraftInstance() {
        return mc;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void selectPlayListIndex(int index) {
        if (index == this.playList_selected_index) {
            return;
        }
        this.playList_selected_index = index;
        this.selectedPlayList = (index >= 0 && index < playList.size()) ? playList.get(playList_selected_index) : null;
    }

    public void selectSubListIndex(int index) {
        if (index == this.subList_selected_index) {
            return;
        }
        this.subList_selected_index = index;
        this.selectedSubList = (index >= 0 && index < subList.size()) ? subList.get(subList_selected_index) : null;
    }

    public void selectTrackIndex(int index) {
        if (index == this.track_selected_index) {
            return;
        }
        this.track_selected_index = index;
        this.selectedTrack = (index >= 0 && index < trackList.size()) ? trackList.get(track_selected_index) : null;
        if (activeView == VIEW_QUEUE && index >= 0 && index < queue.size()) {
            MusicPlaybackManager.getInstance().setQueueIndex(index);
        }
    }

    public boolean playListIndexSelected(int index) {
        return index == playList_selected_index;
    }

    public boolean subListIndexSelected(int index) {
        return index == subList_selected_index;
    }

    public boolean trackIndexSelected(int index) {
        return index == track_selected_index;
    }

    public void LoadTrackList(List<TrackContainer> tracks) {
        trackList.clear();
        if (tracks != null) {
            trackList.addAll(tracks);
        }
        this.track_selected_index = -1;
        this.selectedTrack = null;
    }

    public void openPlayList(PlayListContainer container) {
        if (container == null) {
            MusicPlaybackManager.getInstance().setStatusMessage("请先选择一个歌单");
            return;
        }
        selectedPlayList = container;
        activeSourceLabel = "歌单: " + safeText(container.getName());
        showTracks(activeSourceLabel, getPlayListTracks(container.getId()), "列表为空");
    }

    public void openAlbum(SubListContainer container) {
        if (container == null) {
            MusicPlaybackManager.getInstance().setStatusMessage("请先选择一个专辑");
            return;
        }
        selectedSubList = container;
        activeSourceLabel = "专辑: " + safeText(container.getName());
        showTracks(activeSourceLabel, getAlbumTracks(container.getId()), "列表为空");
    }

    public ArrayList<TrackContainer> getPlayListTracks(long id) {
        return musicManager.LoadTrackList(id);
    }

    public ArrayList<TrackContainer> getAlbumTracks(long id) {
        return musicManager.LoadAlbumTrackList(id);
    }

    public void PlayMusic() {
        MusicPlaybackManager manager = MusicPlaybackManager.getInstance();
        manager.setBlockPos(blockPos);

        if (this.selectedTrack != null) {
            if (activeView != VIEW_QUEUE) {
                manager.getQueue().clear();
                manager.getQueue().addAll(this.trackList);
                manager.setFmMode(false);
            }

            int idx = -1;
            for (int i = 0; i < manager.getQueue().size(); i++) {
                if (manager.getQueue().get(i).getId() == this.selectedTrack.getId()) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                manager.setQueueIndex(idx);
            } else {
                manager.getQueue().add(this.selectedTrack);
                manager.setQueueIndex(manager.getQueue().size() - 1);
            }

            manager.play(selectedTrack);
        } else {
            if (manager.getCurrentTrack() != null) {
                manager.play(manager.getCurrentTrack());
            } else if (!manager.getQueue().isEmpty()) {
                manager.setQueueIndex(0);
                manager.play(manager.getQueue().get(0));
            } else {
                MusicPlaybackManager.getInstance().setStatusMessage("请先选择一首歌");
            }
        }
    }

    private void switchView(int view) {
        activeView = view;
        if (searchField != null) {
            searchField.setFocused(view == VIEW_SEARCH);
        }
        if (view == VIEW_PLAYLISTS) {
            viewTitle = "我的歌单";
            activeSourceLabel = "Library";
            MusicPlaybackManager.getInstance().setStatusMessage(playList.isEmpty() ? "无可用歌单" : "双击打开歌单");
        } else if (view == VIEW_ALBUMS) {
            viewTitle = "收藏的专辑";
            activeSourceLabel = "Albums";
            MusicPlaybackManager.getInstance().setStatusMessage(subList.isEmpty() ? "无收藏专辑" : "双击打开专辑");
        } else if (view == VIEW_SEARCH) {
            viewTitle = "音乐搜索";
            activeSourceLabel = "Search";
            LoadTrackList(searchResults);
            MusicPlaybackManager.getInstance().setStatusMessage(searchResults.isEmpty() ? "输入关键词并回车搜索" : "双击播放，或加至队列");
        } else if (view == VIEW_QUEUE) {
            viewTitle = "播放队列";
            activeSourceLabel = "Queue";
            LoadTrackList(queue);
            MusicPlaybackManager.getInstance().setStatusMessage(queue.isEmpty() ? "播放队列为空" : "双击播放队列中的歌曲");
        }
    }

    private void showTracks(String title, List<TrackContainer> tracks, String emptyText) {
        viewTitle = title;
        activeView = VIEW_TRACKS;
        LoadTrackList(tracks);
        MusicPlaybackManager.getInstance().setStatusMessage(trackList.isEmpty() ? emptyText : "双击播放，或加至队列");
        if (searchField != null) {
            searchField.setFocused(false);
        }
    }

    private void runSearch() {
        String keywords = searchField == null ? "" : searchField.getText();
        if (isBlank(keywords)) {
            MusicPlaybackManager.getInstance().setStatusMessage("请输入搜索词");
            return;
        }
        activeSourceLabel = "搜索: " + keywords.trim();
        viewTitle = activeSourceLabel;
        searchResults.clear();
        searchResults.addAll(musicManager.SearchSongs(keywords));
        LoadTrackList(searchResults);
        activeView = VIEW_SEARCH;
        MusicPlaybackManager.getInstance().setStatusMessage(trackList.isEmpty() ? "未搜到相关歌曲" : "双击播放，或加至队列");
        if (searchField != null) {
            searchField.setFocused(true);
        }
    }

    private void addSelectedToQueue() {
        if (selectedTrack == null) {
            MusicPlaybackManager.getInstance().setStatusMessage("请先选择一首歌");
            return;
        }
        MusicPlaybackManager.getInstance().getQueue().add(selectedTrack);
        MusicPlaybackManager.getInstance().setStatusMessage("已加至播放队列: " + safeText(selectedTrack.getName()));
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        if (value == null) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String getModeShortText(MusicPlaybackManager.PlayMode mode) {
        switch (mode) {
            case SEQUENCE: return "顺序";
            case LIST_LOOP: return "循环";
            case SINGLE_LOOP: return "单曲";
            case SHUFFLE: return "随机";
            default: return "循环";
        }
    }

    private String getQualityShortText(String quality) {
        if (quality == null) return "最高";
        quality = quality.trim().toLowerCase();
        if ("highest".equals(quality) || "jysky".equals(quality)) return "最高";
        if ("lossless".equals(quality)) return "无损";
        if ("hires".equals(quality)) return "超高";
        if ("exhigh".equals(quality)) return "极高";
        if ("higher".equals(quality)) return "较高";
        if ("standard".equals(quality)) return "标准";
        return "最高";
    }

    private void toggleQuality() {
        String current = team.info.ncmfm.NcmConfig.audioQuality;
        if (current == null) {
            current = "highest";
        }
        current = current.trim().toLowerCase();
        String next;
        if ("highest".equals(current)) {
            next = "standard";
        } else if ("standard".equals(current)) {
            next = "higher";
        } else if ("higher".equals(current)) {
            next = "exhigh";
        } else if ("exhigh".equals(current)) {
            next = "lossless";
        } else {
            next = "highest";
        }
        team.info.ncmfm.NcmConfig.audioQuality = next;
        try {
            net.minecraftforge.common.config.ConfigManager.sync(team.info.ncmfm.NcmMod.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
        } catch (Exception e) {
            // ignore
        }
        MusicPlaybackManager.getInstance().setStatusMessage("播放音质: " + getQualityShortText(next));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
