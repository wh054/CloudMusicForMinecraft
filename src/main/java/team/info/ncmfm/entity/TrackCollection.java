package team.info.ncmfm.entity;

import java.io.Serializable;

public class TrackCollection implements Serializable {
    private int code;
    private PlayList playlist;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public PlayList getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlayList playlist) {
        this.playlist = playlist;
    }
}

