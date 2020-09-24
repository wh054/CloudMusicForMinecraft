package team.info.ncmfm.entity;

import java.io.Serializable;

public class PlayListCollection implements Serializable {

    private PlayList[] playlist;

    public PlayList[] getPlaylist() {
        return playlist;
    }

    public void setPlaylist(PlayList[] playlist) {
        this.playlist = playlist;
    }
}
