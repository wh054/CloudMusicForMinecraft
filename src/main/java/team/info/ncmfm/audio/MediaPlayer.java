package team.info.ncmfm.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.player.Player;
import net.minecraft.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class MediaPlayer {
    private static final Logger logger = LogManager.getLogger(MediaPlayer.class);
    private String url="";

    private InputStream inputStream=null;
    private BufferedInputStream buffer=null;
    private Bitstream bt=null;

    public MediaPlayer(String url){
        this.url=url;
        logger.info("Ã½ÌåÁ´½Ó ==>> "+url);
    }

    public void play() {
        try{
            if(!StringUtils.isNullOrEmpty(this.url)){
                URL uurl = new URL(this.url);
                URLConnection con = uurl.openConnection();
                inputStream = con.getInputStream();
                buffer = new BufferedInputStream(inputStream);
                bt = new Bitstream(buffer);
                Player player = new Player(buffer);
                player.play();
            }
        }catch (Exception ex){
            logger.error(ex.getMessage());
        }finally {
            if(inputStream!=null){
                try{
                    inputStream.close();
                }catch (IOException ex){
                    logger.error(ex.getMessage());
                }
            }
            if(buffer!=null){
                try{
                    buffer.close();
                }catch (IOException ex){
                    logger.error(ex.getMessage());
                }
            }
            if(bt!=null){
                try{
                    bt.close();
                }catch (BitstreamException ex){
                    logger.error(ex.getMessage());
                }
            }
        }
    };

}
