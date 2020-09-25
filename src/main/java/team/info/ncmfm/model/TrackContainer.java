package team.info.ncmfm.model;

public class TrackContainer {
    public long id;
    public String name;
    public String album;
    public String author;

    public  TrackContainer(long id, String name){
        this.id=id;
        this.name=name;
    }

    public  TrackContainer(long id, String name,String author,String album){
        this.id=id;
        this.name=name;
        this.album=album;
        this.author=author;
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
