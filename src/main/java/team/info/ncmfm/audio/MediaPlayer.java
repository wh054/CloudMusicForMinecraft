package team.info.ncmfm.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;

public abstract class MediaPlayer {
    public InputStream mediaStream;

    public MediaPlayer(InputStream stream){
        this.mediaStream=stream;
    }

    public void play() {
        BufferedInputStream buffer = new BufferedInputStream(this.mediaStream);
        Bitstream bt = new Bitstream(buffer);
        try{
            Player player = new Player(buffer);
            player.play();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    };

}
