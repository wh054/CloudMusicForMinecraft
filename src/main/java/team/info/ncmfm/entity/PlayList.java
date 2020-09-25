package team.info.ncmfm.entity;

import java.io.Serializable;
import java.util.List;

public class PlayList implements Serializable {
    private long id;
    private String name;
    private int trackCount;
    private List<Tracks> tracks;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public List<Tracks> getTracks() {
        return tracks;
    }

    public void setTracks(List<Tracks> tracks) {
        this.tracks = tracks;
    }

    public class Tracks implements Serializable{
        private long id;
        private String name;
        private List<Author> ar;
        private Album al;

        public List<Author> getAr() {
            return ar;
        }

        public void setAr(List<Author> ar) {
            this.ar = ar;
        }

        public Album getAl() {
            return al;
        }

        public void setAl(Album al) {
            this.al = al;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public class Author implements Serializable{
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public class Album implements Serializable{
            private long id;
            private String name;
            private String picUrl;

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPicUrl() {
                return picUrl;
            }

            public void setPicUrl(String picUrl) {
                this.picUrl = picUrl;
            }
        }
    }
}
