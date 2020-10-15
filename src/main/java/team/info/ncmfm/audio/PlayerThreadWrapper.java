package team.info.ncmfm.audio;

import javazoom.jl.player.Player;
import net.minecraft.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.NcmMod;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class PlayerThreadWrapper implements Runnable{
    private static final Logger logger = LogManager.getLogger(PlayerThreadWrapper.class);
    private String url="";

    private InputStream inputStream;
    private BufferedInputStream buffer;

    public PlayerThreadWrapper(String url) {
        this.url=url;
    }

    @Override
    public void run() {
        try{
            if(!StringUtils.isNullOrEmpty(url)){
                URL uurl = new URL(url);
                URLConnection con = uurl.openConnection();
                inputStream = con.getInputStream();
                buffer = new BufferedInputStream(inputStream);
                NcmMod.mp3Player = new Player(buffer);
                NcmMod.mp3Player.play();
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
        }
    }
}
