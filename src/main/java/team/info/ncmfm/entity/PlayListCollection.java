package team.info.ncmfm.entity;

import java.io.Serializable;
import java.util.List;

public class PlayListCollection implements Serializable {

    private List<PlayList> playlist;

    public List<PlayList> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<PlayList> playlist) {
        this.playlist = playlist;
    }
}
