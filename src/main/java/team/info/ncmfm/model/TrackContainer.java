package team.info.ncmfm.model;

public class TrackContainer {
    public long id;
    public String name;

    public  TrackContainer(long id,String name){
        this.id=id;
        this.name=name;
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
}
