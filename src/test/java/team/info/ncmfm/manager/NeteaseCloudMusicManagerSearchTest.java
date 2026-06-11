package team.info.ncmfm.manager;

import org.junit.Test;
import team.info.ncmfm.entity.SearchSongs;
import team.info.ncmfm.model.TrackContainer;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NeteaseCloudMusicManagerSearchTest {
    @Test
    public void convertsSearchSongsToTrackContainers() {
        SearchSongs response = new SearchSongs();
        response.setCode(200);

        SearchSongs.Artist artist = new SearchSongs.Artist();
        artist.setName("Artist Name");

        SearchSongs.Album album = new SearchSongs.Album();
        album.setName("Album Name");

        SearchSongs.Song song = new SearchSongs.Song();
        song.setId(12345L);
        song.setName("Song Name");
        song.setArtists(Arrays.asList(artist));
        song.setAlbum(album);

        SearchSongs.Result result = new SearchSongs.Result();
        result.setSongs(Arrays.asList(song));
        response.setResult(result);

        ArrayList<TrackContainer> tracks = NeteaseCloudMusicManager.toSearchTrackContainers(response);

        assertEquals(1, tracks.size());
        assertEquals(12345L, tracks.get(0).getId());
        assertEquals("Song Name", tracks.get(0).getName());
        assertEquals("Artist Name", tracks.get(0).getAuthor());
        assertEquals("Album Name", tracks.get(0).getAlbum());
    }

    @Test
    public void returnsEmptyTracksForMalformedSearchResponse() {
        SearchSongs response = new SearchSongs();
        response.setCode(500);

        ArrayList<TrackContainer> tracks = NeteaseCloudMusicManager.toSearchTrackContainers(response);

        assertTrue(tracks.isEmpty());
    }
}
