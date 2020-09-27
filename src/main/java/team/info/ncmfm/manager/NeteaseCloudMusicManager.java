package team.info.ncmfm.manager;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import team.info.ncmfm.entity.MusicPacket;
import team.info.ncmfm.entity.PlayList;
import team.info.ncmfm.entity.PlayListCollection;
import team.info.ncmfm.entity.TrackCollection;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class NeteaseCloudMusicManager implements IMusicManager {
    private final String HOST="http://182.254.171.36:3000";

    @Override
    public ArrayList<PlayListContainer> LoadPlayList() {
        ArrayList<PlayListContainer> as=new ArrayList<>();
        PlayListCollection playListCollection= GetPlayListByUid(53825510);
        if(playListCollection!=null){
            for(PlayList temp: playListCollection.getPlaylist()){
                as.add(new PlayListContainer(temp.getId(),temp.getName()));
            }
        }
        return as;
    }

    @Override
    public ArrayList<TrackContainer> LoadTrackList(long id) {
        ArrayList<TrackContainer> as=new ArrayList<>();
        TrackCollection trackCollection=GetTracksById(id);
        if(trackCollection!=null){
            for(PlayList.Tracks temp: trackCollection.getPlaylist().getTracks()){
                as.add(new TrackContainer(
                        temp.getId(),
                        temp.getName(),
                        temp.getAr().get(0).getName(),
                        temp.getAl().getName()
                ));
            }
        }
        return as;
    }

    @Override
    public String GetMusicById(long id) {
        String url=HOST+"/song/url?id="+id+"&br=128000";
        String musicUrl="";
        MusicPacket packet=doGet(MusicPacket.class,url);
        if(packet.getCode()==200){
            musicUrl=packet.getData().get(0).getUrl();
        }
        return musicUrl;
    }

    private PlayListCollection GetPlayListByUid(int uid){
        String url=HOST+"/user/playlist?uid="+uid;
        return doGet(PlayListCollection.class,url);
    }

    private TrackCollection GetTracksById(long id){
        String url=HOST+"/playlist/detail?id="+id;
        return doGet(TrackCollection.class,url);
    }

    private <T> T doGet(Class <T> t,String url){
        T rs=null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        try {
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            HttpResponse response = client.execute(get);
            String jsonResult = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            rs=gson.fromJson(jsonResult,(Type)t);
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }
}
