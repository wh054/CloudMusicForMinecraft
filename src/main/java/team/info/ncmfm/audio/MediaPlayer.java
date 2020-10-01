package team.info.ncmfm.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;

public abstract class MediaPlayer {
    public InputStream mediaStream;

    public MediaPlayer(InputStream stream){
        this.mediaStream=stream;
    }

    public void play() throws JavaLayerException {
        BufferedInputStream buffer = new BufferedInputStream(this.mediaStream);
        Bitstream bt = new Bitstream(buffer);
        Player player = new Player(buffer);
        player.play();
    };

}
