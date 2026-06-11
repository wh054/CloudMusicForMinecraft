package team.info.ncmfm.manager;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.entity.PersonalFM;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.MusicInfoWrapper;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.net.EnumMusicCommand;
import team.info.ncmfm.net.MusicMessage;
import team.info.ncmfm.net.MusicPacketHandler;
import team.info.ncmfm.proxy.ClientProxy;
import team.info.ncmfm.utils.EncryptUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MusicPlaybackManager {
    private static final Logger logger = LogManager.getLogger(MusicPlaybackManager.class);
    private static MusicPlaybackManager INSTANCE;

    public enum PlayMode {
        SEQUENCE("顺序播放"),
        LIST_LOOP("列表循环"),
        SINGLE_LOOP("单曲循环"),
        SHUFFLE("随机播放");

        private final String displayName;
        PlayMode(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }

    public static class LyricLine {
        public long timeMs;
        public String text;
        public LyricLine(long timeMs, String text) {
            this.timeMs = timeMs;
            this.text = text;
        }
    }

    private final IMusicManager musicManager;
    private final List<TrackContainer> queue = new ArrayList<TrackContainer>();
    private final List<LyricLine> lyrics = new ArrayList<LyricLine>();
    private volatile String currentLyricText = "";
    private int queueIndex = -1;
    private PlayMode playMode = PlayMode.LIST_LOOP;
    private float volume = 0.6f;

    private boolean isFmMode = false;
    private final List<TrackContainer> fmQueue = new ArrayList<TrackContainer>();
    private volatile boolean isFetchingFm = false;

    private TrackContainer currentTrack = null;
    private String activeSourceLabel = "Library";
    private String statusMessage = "Ready";
    private boolean isPlaying = false;
    private int ticksSinceStart = 0;
    private boolean isActivePlayer = false;
    private BlockPos currentPos = null;

    private int progressTicks = 0;
    private int errorTicks = 0;
    private boolean hasError = false;

    private MusicPlaybackManager() {
        this.musicManager = new NeteaseCloudMusicManager();
    }

    public static synchronized MusicPlaybackManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MusicPlaybackManager();
        }
        return INSTANCE;
    }

    public void setBlockPos(BlockPos pos) {
        this.currentPos = pos;
    }

    public BlockPos getBlockPos() {
        return currentPos;
    }

    public List<TrackContainer> getQueue() {
        return queue;
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(int index) {
        this.queueIndex = index;
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public void togglePlayMode() {
        PlayMode[] modes = PlayMode.values();
        playMode = modes[(playMode.ordinal() + 1) % modes.length];
        statusMessage = "播放模式: " + playMode.getDisplayName();
    }

    public float getVolume() {
        return volume;
    }

    public void changeVolume(float delta) {
        this.volume = Math.max(0.0f, Math.min(1.0f, this.volume + delta));
        updateSoundSystemVolume();
        statusMessage = "音量: " + Math.round(this.volume * 100) + "%";
    }

    private void updateSoundSystemVolume() {
        if (ClientProxy.soundSystem != null && isPlaying) {
            String sourceId = getSourceId();
            ClientProxy.soundSystem.setVolume(sourceId, this.volume);
        }
    }

    public boolean isFmMode() {
        return isFmMode;
    }

    public void setFmMode(boolean fmMode) {
        this.isFmMode = fmMode;
        if (fmMode) {
            activeSourceLabel = "私人 FM";
        }
    }

    public TrackContainer getCurrentTrack() {
        return currentTrack;
    }

    public String getActiveSourceLabel() {
        return activeSourceLabel;
    }

    public void setActiveSourceLabel(String label) {
        this.activeSourceLabel = label;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getProgressTicks() {
        return progressTicks;
    }

    public void setProgressTicks(int ticks) {
        this.progressTicks = ticks;
    }

    public void play(TrackContainer track) {
        if (track == null) {
            statusMessage = "未选择歌曲";
            return;
        }

        this.currentTrack = track;
        this.ticksSinceStart = 0;
        this.progressTicks = 0;
        this.errorTicks = 0;
        this.hasError = false;
        this.isPlaying = true;
        this.isActivePlayer = true;

        parseLyrics(null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String lrc = musicManager.getLyricById(track.getId());
                    parseLyrics(lrc);
                } catch (Exception e) {
                    logger.error("Failed to fetch lyrics: " + e.getMessage());
                }
            }
        }, "ncmfm-lyrics-fetcher").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = musicManager.GetMusicById(track.getId());
                    if (url == null || url.trim().length() == 0) {
                        statusMessage = "播放失败: 无法获取流媒体地址 (可能需要VIP)";
                        hasError = true;
                        return;
                    }

                    MusicInfoWrapper packet = new MusicInfoWrapper();
                    packet.setCommand(EnumMusicCommand.PLAY);
                    packet.setSource(url);
                    if (currentPos != null) {
                        packet.setPos(currentPos);
                    }

                    statusMessage = "正在播放: " + track.getName();
                    MusicPacketHandler.INSTANCE.sendToServer(new MusicMessage(new Gson().toJson(packet)));

                    // 延迟一小会儿，在主线程更新音量
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            updateSoundSystemVolume();
                        }
                    });

                } catch (Exception e) {
                    logger.error("Failed to play track via manager: " + e.getMessage());
                    statusMessage = "播放出错: " + e.getMessage();
                    hasError = true;
                }
            }
        }, "ncmfm-play-thread").start();
    }

    public void stop() {
        this.isPlaying = false;
        this.isActivePlayer = false;
        this.currentTrack = null;
        this.progressTicks = 0;
        statusMessage = "播放已停止";

        MusicInfoWrapper packet = new MusicInfoWrapper();
        packet.setCommand(EnumMusicCommand.STOP);
        if (currentPos != null) {
            packet.setPos(currentPos);
        }
        MusicPacketHandler.INSTANCE.sendToServer(new MusicMessage(new Gson().toJson(packet)));
    }

    public void next() {
        if (isFmMode) {
            playNextFm();
        } else {
            playNextQueue();
        }
    }

    public void previous() {
        if (isFmMode) {
            statusMessage = "私人 FM 不支持上一首";
            return;
        }
        if (queue.isEmpty()) {
            statusMessage = "播放队列为空";
            return;
        }

        if (playMode == PlayMode.SHUFFLE) {
            playNextQueue(); // 随机模式下上一首也是随机的
        } else {
            queueIndex = queueIndex <= 0 ? queue.size() - 1 : queueIndex - 1;
            play(queue.get(queueIndex));
        }
    }

    private void playNextQueue() {
        if (queue.isEmpty()) {
            statusMessage = "播放队列为空";
            stop();
            return;
        }

        switch (playMode) {
            case SINGLE_LOOP:
                if (currentTrack != null) {
                    play(currentTrack);
                    return;
                }
                // Fallthrough
            case LIST_LOOP:
                queueIndex = (queueIndex + 1) % queue.size();
                play(queue.get(queueIndex));
                break;
            case SEQUENCE:
                queueIndex++;
                if (queueIndex >= 0 && queueIndex < queue.size()) {
                    play(queue.get(queueIndex));
                } else {
                    statusMessage = "队列播放结束";
                    stop();
                }
                break;
            case SHUFFLE:
                queueIndex = new Random().nextInt(queue.size());
                play(queue.get(queueIndex));
                break;
        }
    }

    public void playPersonalFm() {
        isFmMode = true;
        activeSourceLabel = "私人 FM";
        playNextFm();
    }

    private void playNextFm() {
        if (fmQueue.isEmpty()) {
            statusMessage = "正在加载私人 FM...";
            fetchFmTracks(true); // 同步等待或显示加载
        } else {
            TrackContainer track = fmQueue.remove(0);
            play(track);
            checkFmQueueAndFetch();
        }
    }

    private synchronized void checkFmQueueAndFetch() {
        if (fmQueue.size() <= 2 && !isFetchingFm) {
            fetchFmTracks(false);
        }
    }

    private void fetchFmTracks(final boolean playImmediately) {
        if (isFetchingFm) return;
        isFetchingFm = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PersonalFM fm = musicManager.personalFm();
                    if (fm != null && fm.getData() != null && !fm.getData().isEmpty()) {
                        for (PersonalFM.DataBean data : fm.getData()) {
                            if (data != null) {
                                String author = "";
                                if (data.getArtists() != null && !data.getArtists().isEmpty() && data.getArtists().get(0) != null) {
                                    author = data.getArtists().get(0).getName();
                                }
                                String album = "";
                                if (data.getAlbum() != null) {
                                    album = data.getAlbum().getName();
                                }
                                fmQueue.add(new TrackContainer(data.getId(), data.getName(), author, album, data.getDuration()));
                            }
                        }
                        if (playImmediately && !fmQueue.isEmpty()) {
                            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                                @Override
                                public void run() {
                                    playNextFm();
                                }
                            });
                        }
                    } else {
                        statusMessage = "加载私人 FM 失败";
                    }
                } catch (Exception e) {
                    logger.error("Failed to fetch FM tracks: " + e.getMessage());
                } finally {
                    isFetchingFm = false;
                }
            }
        }, "ncmfm-fm-fetcher").start();
    }

    private String getSourceId() {
        return currentPos != null ? EncryptUtil.MD5(currentPos.toString()) + ".MonoMp3" : "background.StereoMp3";
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (isPlaying) {
                ticksSinceStart++;
                progressTicks++;

                // 容错处理: 无法获取流媒体等导致的错误
                if (hasError) {
                    errorTicks++;
                    if (errorTicks >= 20) { // 停留一秒钟提示错误，然后切歌
                        hasError = false;
                        errorTicks = 0;
                        next();
                    }
                    return;
                }

                // 状态检测: 自动切歌
                if (isActivePlayer && ticksSinceStart > 60) { // 60tick (3秒) 的缓冲宽限期
                    if (ClientProxy.soundSystem != null) {
                        String sourceId = getSourceId();
                        if (!ClientProxy.soundSystem.playing(sourceId)) {
                            logger.info("Track finished or source stopped: " + sourceId + ". Autoplaying next.");
                            next();
                        }
                    }
                }

                // 更新当前歌词
                String foundText = "";
                long elapsedMs = progressTicks * 50;
                synchronized (this.lyrics) {
                    for (LyricLine line : this.lyrics) {
                        if (line.timeMs <= elapsedMs) {
                            foundText = line.text;
                        } else {
                            break;
                        }
                    }
                }
                this.currentLyricText = foundText;
            }
        }
    }

    public String getCurrentLyricText() {
        return currentLyricText;
    }

    private void parseLyrics(String rawLrc) {
        synchronized (this.lyrics) {
            this.lyrics.clear();
            this.currentLyricText = "";
            if (rawLrc == null || rawLrc.trim().length() == 0) {
                return;
            }

            try {
                String[] lines = rawLrc.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    int openIdx = line.indexOf('[');
                    int closeIdx = line.indexOf(']');
                    if (openIdx >= 0 && closeIdx > openIdx) {
                        String timeStr = line.substring(openIdx + 1, closeIdx);
                        String text = line.substring(closeIdx + 1).trim();

                        // Clean unicode spaces and control characters that Minecraft FontRenderer doesn't support
                        text = text.replace('\u3000', ' ');
                        text = text.replaceAll("[\\u2000-\\u200A\\u202F\\u205F]", " ");
                        text = text.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
                        text = text.trim();

                        if (text.isEmpty() || timeStr.startsWith("by:") || timeStr.startsWith("al:") || timeStr.startsWith("ar:") || timeStr.startsWith("ti:") || timeStr.startsWith("offset:")) {
                            continue;
                        }

                        long timeMs = parseLrcTime(timeStr);
                        if (timeMs >= 0) {
                            this.lyrics.add(new LyricLine(timeMs, text));
                        }
                    }
                }
                java.util.Collections.sort(this.lyrics, new java.util.Comparator<LyricLine>() {
                    @Override
                    public int compare(LyricLine o1, LyricLine o2) {
                        return Long.compare(o1.timeMs, o2.timeMs);
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to parse lyrics: " + e.getMessage());
            }
        }
    }

    private long parseLrcTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            long min = Long.parseLong(parts[0]);
            String[] secParts = parts[1].split("\\.");
            long sec = Long.parseLong(secParts[0]);
            long ms = 0;
            if (secParts.length > 1) {
                String msStr = secParts[1];
                if (msStr.length() == 2) {
                    ms = Long.parseLong(msStr) * 10;
                } else if (msStr.length() == 3) {
                    ms = Long.parseLong(msStr);
                } else {
                    ms = Long.parseLong(msStr.substring(0, 3));
                }
            }
            return min * 60000 + sec * 1000 + ms;
        } catch (Exception e) {
            return -1;
        }
    }
}
