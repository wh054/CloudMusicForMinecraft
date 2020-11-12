package team.info.ncmfm.model;

public class SubListContainer {
    private long id;
    private String name;

    public SubListContainer(long id,String name){
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
