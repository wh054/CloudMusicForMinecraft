package team.info.ncmfm.entity;

import java.io.Serializable;
import java.util.List;

public class PlayListCollection implements Serializable {

    private int code;
    private List<PlayList> playlist;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<PlayList> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<PlayList> playlist) {
        this.playlist = playlist;
    }
}

