package team.info.ncmfm.manager;

import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;

import java.util.ArrayList;

public class NeteaseCloudMusicManager implements IMusicManager {
    @Override
    public ArrayList<PlayListContainer> LoadPlayList() {
        ArrayList<PlayListContainer> as=new ArrayList<>();
        for(int i=0;i<10;i++){
            PlayListContainer temp=new PlayListContainer();
            temp.setName("��ɱ���ء����������� LEVEL-"+i);
            as.add(temp);
        }
        return as;
    }
}
