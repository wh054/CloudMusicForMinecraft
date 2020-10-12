package team.info.ncmfm.entity;

import java.io.Serializable;
import java.util.List;

public class PlayList implements Serializable {
    private long id;
    private String name;
    private int trackCount;
    private List<Tracks> tracks;
    private List<TrackId> trackids;

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

    public List<TrackId> getTrackids() {
        return trackids;
    }

    public void setTrackids(List<TrackId> trackids) {
        this.trackids = trackids;
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

    public class TrackId implements Serializable{
        private int id;
        private int v;
        private long at;
        private Object alg;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getV() {
            return v;
        }

        public void setV(int v) {
            this.v = v;
        }

        public long getAt() {
            return at;
        }

        public void setAt(long at) {
            this.at = at;
        }

        public Object getAlg() {
            return alg;
        }

        public void setAlg(Object alg) {
            this.alg = alg;
        }
    }
}
