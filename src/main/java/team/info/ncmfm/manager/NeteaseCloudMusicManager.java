package team.info.ncmfm.manager;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import team.info.ncmfm.entity.PlayList;
import team.info.ncmfm.entity.PlayListCollection;
import team.info.ncmfm.entity.TrackCollection;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.io.IOException;
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
                as.add(new TrackContainer(temp.getId(),temp.getName()));
            }
        }
        return as;

    }

    private PlayListCollection GetPlayListByUid(int uid){
        PlayListCollection rs=null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(HOST+"/user/playlist?uid="+uid);
        try {
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            HttpResponse response = client.execute(get);
            String jsonResult = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            rs=gson.fromJson(jsonResult, PlayListCollection.class);
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    private TrackCollection GetTracksById(long id){
        TrackCollection rs=null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(HOST+"/playlist/detail?id="+id);
        try {
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            HttpResponse response = client.execute(get);
            String jsonResult = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            rs=gson.fromJson(jsonResult, TrackCollection.class);
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }
}
