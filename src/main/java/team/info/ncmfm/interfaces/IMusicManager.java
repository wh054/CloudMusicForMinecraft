package team.info.ncmfm.interfaces;

import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.util.ArrayList;

public interface IMusicManager {
    ArrayList<PlayListContainer> LoadPlayList();

    ArrayList<TrackContainer> LoadTrackList(long id);
}
