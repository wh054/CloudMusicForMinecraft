package team.info.ncmfm.entity;

import java.io.Serializable;

public class PlayList implements Serializable {
    private long id;
    private String name;
    private int trackCount;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }
}
