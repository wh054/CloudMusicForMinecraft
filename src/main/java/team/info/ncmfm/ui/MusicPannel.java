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
import team.info.ncmfm.entity.PersonalFM;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.MusicInfoWrapper;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.net.EnumMusicCommand;
import team.info.ncmfm.net.MusicMessage;
import team.info.ncmfm.net.MusicPacketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MusicPannel extends GuiScreen {
    public final int width;
    public final int height;

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
    private int queueIndex = -1;
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
    private String currentSource = "Ready";

    private PlayListContainer selectedPlayList;
    private SubListContainer selectedSubList;
    private TrackContainer selectedTrack;
    private TrackContainer currentTrack;

    private final IMusicManager musicManager;
    private BlockPos blockPos;

    public MusicPannel(Minecraft mc, IMusicManager musicManager) {
        playList = new ArrayList<PlayListContainer>();
        trackList = new ArrayList<TrackContainer>();
        searchResults = new ArrayList<TrackContainer>();
        subList = new ArrayList<SubListContainer>();
        queue = new ArrayList<TrackContainer>();
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
        this.musicManager = musicManager;
    }

    public MusicPannel(Minecraft mc, IMusicManager musicManager, BlockPos pos) {
        playList = new ArrayList<PlayListContainer>();
        trackList = new ArrayList<TrackContainer>();
        searchResults = new ArrayList<TrackContainer>();
        subList = new ArrayList<SubListContainer>();
        queue = new ArrayList<TrackContainer>();
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
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
        panelWidth = Math.max(320, Math.min(width - 28, 460));
        panelHeight = Math.max(230, Math.min(height - 24, 286));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        sidebarWidth = 94;
        headerHeight = 30;
        playerHeight = 54;
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
        buttonList.add(new GuiButton(BUTTON_NAV_PLAYLISTS, navLeft, navTop, navWidth, 18, "Playlists"));
        buttonList.add(new GuiButton(BUTTON_NAV_ALBUMS, navLeft, navTop + 22, navWidth, 18, "Albums"));
        buttonList.add(new GuiButton(BUTTON_NAV_SEARCH, navLeft, navTop + 44, navWidth, 18, "Search"));
        buttonList.add(new GuiButton(BUTTON_PERSONAL_FM, navLeft, navTop + 66, navWidth, 18, "Private FM"));
        buttonList.add(new GuiButton(BUTTON_NAV_QUEUE, navLeft, navTop + 88, navWidth, 18, "Queue"));

        buttonList.add(new GuiButton(BUTTON_REFLASH_STATE, panelLeft + panelWidth - 112, panelTop + 6, 52, 18, "Refresh"));
        buttonList.add(new GuiButton(BUTTON_CLOSE, panelLeft + panelWidth - 56, panelTop + 6, 46, 18, "Close"));

        buttonList.add(new GuiButton(BUTTON_SEARCH, contentLeft + contentWidth - 58, contentTop, 58, 18, "Search"));
        buttonList.add(new GuiButton(BUTTON_ADD_QUEUE, contentLeft + contentWidth - 58, contentTop, 58, 18, "+Queue"));

        int controlsY = panelTop + panelHeight - 42;
        int controlsRight = panelLeft + panelWidth - 10;
        buttonList.add(new GuiButton(BUTTON_PREVIOUS, controlsRight - 180, controlsY, 42, 20, "Prev"));
        buttonList.add(new GuiButton(BUTTON_PLAY_SELECTED, controlsRight - 134, controlsY, 42, 20, "Play"));
        buttonList.add(new GuiButton(BUTTON_NEXT, controlsRight - 88, controlsY, 36, 20, "Next"));
        buttonList.add(new GuiButton(BUTTON_STOP_MUSIC, controlsRight - 48, controlsY, 38, 20, "Stop"));

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
        if (statusMessage.length() == 0) {
            statusMessage = "Library loaded";
        }
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
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xDD101820);
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + headerHeight, 0xEE18202A);
        drawRect(panelLeft, panelTop + headerHeight, panelLeft + sidebarWidth, panelTop + panelHeight - playerHeight, 0xAA121923);
        drawRect(panelLeft, panelTop + panelHeight - playerHeight, panelLeft + panelWidth, panelTop + panelHeight, 0xEE18202A);
        drawRect(contentLeft - 4, contentTop - 4, contentLeft + contentWidth + 4, contentTop + contentHeight + 4, 0x66101820);

        drawString(fontRenderer, "NetEase Cloud Music", panelLeft + 10, panelTop + 10, 0xFFFFFF);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(viewTitle, contentWidth - 118), contentLeft, panelTop + 10, 0xFFFFFF);
        drawString(fontRenderer, blockPos == null ? "Background" : "Block Source", panelLeft + 10, panelTop + panelHeight - 14, 0x888888);
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
            drawRect(panelLeft + 5, selectedY - 2, panelLeft + sidebarWidth - 5, selectedY + 20, 0x55345678);
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
        String message = statusMessage;
        if (activeView == VIEW_PLAYLISTS && playList.isEmpty()) {
            message = "No playlists loaded";
        } else if (activeView == VIEW_ALBUMS && subList.isEmpty()) {
            message = "No albums loaded";
        } else if ((activeView == VIEW_TRACKS || activeView == VIEW_SEARCH || activeView == VIEW_QUEUE) && trackList.isEmpty()) {
            message = statusMessage.length() == 0 ? "No tracks found" : statusMessage;
        }

        if (message.length() > 0) {
            int y = activeView == VIEW_SEARCH ? contentTop + 22 : contentTop;
            drawString(fontRenderer, fontRenderer.trimStringToWidth(message, contentWidth - 8), contentLeft, y, 0xAAAAAA);
        }
    }

    private void drawPlayerBar() {
        int playerTop = panelTop + panelHeight - playerHeight;
        String title = currentTrack == null ? "No track playing" : safeText(currentTrack.getName());
        String meta = currentTrack == null ? currentSource : trackMeta(currentTrack);
        int textWidth = Math.max(80, panelWidth - 205);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(title, textWidth), panelLeft + 10, playerTop + 12, 0xFFFFFF);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(meta, textWidth), panelLeft + 10, playerTop + 28, 0xAAAAAA);
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
                StopMusic();
                break;
            case BUTTON_PERSONAL_FM:
                playPersonalFm();
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
                playPrevious();
                break;
            case BUTTON_PLAY_SELECTED:
                PlayMusic();
                break;
            case BUTTON_NEXT:
                playNext();
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
            queueIndex = index;
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
            statusMessage = "Select a playlist first";
            return;
        }
        selectedPlayList = container;
        activeSourceLabel = "Playlist: " + safeText(container.getName());
        showTracks(activeSourceLabel, getPlayListTracks(container.getId()), "No tracks found");
    }

    public void openAlbum(SubListContainer container) {
        if (container == null) {
            statusMessage = "Select an album first";
            return;
        }
        selectedSubList = container;
        activeSourceLabel = "Album: " + safeText(container.getName());
        showTracks(activeSourceLabel, getAlbumTracks(container.getId()), "No tracks found");
    }

    public ArrayList<TrackContainer> getPlayListTracks(long id) {
        return musicManager.LoadTrackList(id);
    }

    public ArrayList<TrackContainer> getAlbumTracks(long id) {
        return musicManager.LoadAlbumTrackList(id);
    }

    public void PlayMusic() {
        if (this.selectedTrack == null) {
            statusMessage = "Select a song first";
            return;
        }
        MusicInfoWrapper packet = new MusicInfoWrapper();
        packet.setCommand(EnumMusicCommand.PLAY);
        try {
            String musicUrl = musicManager.GetMusicById(this.selectedTrack.getId());
            if (isBlank(musicUrl)) {
                statusMessage = "Play failed: no stream url";
                return;
            }
            packet.setSource(musicUrl);

            if (blockPos != null) {
                packet.setPos(this.blockPos);
            }
            currentTrack = selectedTrack;
            currentSource = activeSourceLabel;
            statusMessage = "Playing: " + safeText(this.selectedTrack.getName());
            syncQueueIndexForSelected();
            MusicPacketHandler.INSTANCE.sendToServer(new MusicMessage(new Gson().toJson(packet)));
        } catch (Exception e) {
            statusMessage = "Play failed: " + e.getMessage();
        }
    }

    public void StopMusic() {
        MusicInfoWrapper packet = new MusicInfoWrapper();
        packet.setCommand(EnumMusicCommand.STOP);
        if (this.blockPos != null) {
            packet.setPos(this.blockPos);
        }
        statusMessage = "Playback stopped";
        currentSource = "Stopped";
        MusicPacketHandler.INSTANCE.sendToServer(new MusicMessage(new Gson().toJson(packet)));
    }

    private void switchView(int view) {
        activeView = view;
        if (searchField != null) {
            searchField.setFocused(view == VIEW_SEARCH);
        }
        if (view == VIEW_PLAYLISTS) {
            viewTitle = "My Playlists";
            activeSourceLabel = "Library";
            statusMessage = playList.isEmpty() ? "No playlists loaded" : "Double-click a playlist";
        } else if (view == VIEW_ALBUMS) {
            viewTitle = "Subscribed Albums";
            activeSourceLabel = "Albums";
            statusMessage = subList.isEmpty() ? "No albums loaded" : "Double-click an album";
        } else if (view == VIEW_SEARCH) {
            viewTitle = "Search";
            activeSourceLabel = "Search";
            LoadTrackList(searchResults);
            statusMessage = searchResults.isEmpty() ? "Type keywords and press Search" : "Double-click a song";
        } else if (view == VIEW_QUEUE) {
            viewTitle = "Queue";
            activeSourceLabel = "Queue";
            LoadTrackList(queue);
            statusMessage = queue.isEmpty() ? "Queue is empty" : "Double-click a queued track";
        }
    }

    private void showTracks(String title, List<TrackContainer> tracks, String emptyText) {
        viewTitle = title;
        activeView = VIEW_TRACKS;
        LoadTrackList(tracks);
        statusMessage = trackList.isEmpty() ? emptyText : "Double-click a song, or add it to queue";
        if (searchField != null) {
            searchField.setFocused(false);
        }
    }

    private void runSearch() {
        String keywords = searchField == null ? "" : searchField.getText();
        if (isBlank(keywords)) {
            statusMessage = "Type keywords and press Search";
            return;
        }
        activeSourceLabel = "Search: " + keywords.trim();
        viewTitle = activeSourceLabel;
        searchResults.clear();
        searchResults.addAll(musicManager.SearchSongs(keywords));
        LoadTrackList(searchResults);
        activeView = VIEW_SEARCH;
        statusMessage = trackList.isEmpty() ? "No tracks found" : "Double-click a song, or add it to queue";
        if (searchField != null) {
            searchField.setFocused(true);
        }
    }

    private void addSelectedToQueue() {
        if (selectedTrack == null) {
            statusMessage = "Select a song first";
            return;
        }
        queue.add(selectedTrack);
        if (queueIndex < 0) {
            queueIndex = 0;
        }
        statusMessage = "Added to queue: " + safeText(selectedTrack.getName());
    }

    private void playNext() {
        if (queue.isEmpty()) {
            statusMessage = "Queue is empty";
            return;
        }
        queueIndex = queueIndex < 0 ? 0 : (queueIndex + 1) % queue.size();
        selectedTrack = queue.get(queueIndex);
        activeSourceLabel = "Queue";
        PlayMusic();
    }

    private void playPrevious() {
        if (queue.isEmpty()) {
            statusMessage = "Queue is empty";
            return;
        }
        queueIndex = queueIndex <= 0 ? queue.size() - 1 : queueIndex - 1;
        selectedTrack = queue.get(queueIndex);
        activeSourceLabel = "Queue";
        PlayMusic();
    }

    private void playPersonalFm() {
        PersonalFM pm = musicManager.personalFm();
        if (pm == null || pm.getData() == null || pm.getData().isEmpty() || pm.getData().get(0) == null) {
            statusMessage = "Private FM load failed";
            return;
        }
        PersonalFM.DataBean data = pm.getData().get(0);
        selectedTrack = new TrackContainer(data.getId(), data.getName());
        activeSourceLabel = "Private FM";
        PlayMusic();
    }

    private void syncQueueIndexForSelected() {
        if (selectedTrack == null) {
            return;
        }
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i) != null && queue.get(i).getId() == selectedTrack.getId()) {
                queueIndex = i;
                return;
            }
        }
    }

    private String trackMeta(TrackContainer track) {
        String artist = safeText(track.getAuthor());
        String album = safeText(track.getAlbum());
        if (artist.length() == 0 && album.length() == 0) {
            return currentSource;
        }
        if (artist.length() == 0) {
            return album;
        }
        if (album.length() == 0) {
            return artist;
        }
        return artist + " - " + album;
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

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
