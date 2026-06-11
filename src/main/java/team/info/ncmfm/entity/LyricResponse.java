package team.info.ncmfm.entity;

import java.io.Serializable;

public class LyricResponse implements Serializable {
    private int code;
    private LrcBean lrc;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public LrcBean getLrc() {
        return lrc;
    }

    public void setLrc(LrcBean lrc) {
        this.lrc = lrc;
    }

    public static class LrcBean implements Serializable {
        private String lyric;

        public String getLyric() {
            return lyric;
        }

        public void setLyric(String lyric) {
            this.lyric = lyric;
        }
    }
}
