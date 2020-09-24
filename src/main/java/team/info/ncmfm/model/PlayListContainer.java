package team.info.ncmfm.model;

public class PlayListContainer {
    public long id;
    public String name;

    public PlayListContainer(long id,String name){
        this.setId(id);
        this.setName(name);
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
