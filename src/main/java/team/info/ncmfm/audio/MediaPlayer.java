package team.info.ncmfm.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class MediaPlayer {
    private String url="";

    private InputStream inputStream=null;
    private BufferedInputStream buffer=null;
    private Bitstream bt=null;

    public MediaPlayer(String url){
        this.url=url;
    }

    public void play() {
        try{
            URL uurl = new URL(this.url);
            URLConnection con = uurl.openConnection();
            inputStream = con.getInputStream();
            buffer = new BufferedInputStream(inputStream);
            bt = new Bitstream(buffer);
            Player player = new Player(buffer);
            player.play();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }finally {
            if(inputStream!=null){
                try{
                    inputStream.close();
                }catch (IOException ex){

                }
            }
            if(buffer!=null){
                try{
                    buffer.close();
                }catch (IOException ex){

                }
            }
            if(bt!=null){
                try{
                    bt.close();
                }catch (BitstreamException ex){

                }
            }
        }
    };

}
