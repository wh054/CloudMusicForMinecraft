package team.info.ncmfm.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.info.ncmfm.audio.Mp3Player;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MusicMessageClientHandler implements IMessageHandler<MusicMessage, IMessage> {
    public static Thread musicThread;

    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if(ctx.side.isClient()){
            if(message.send.startsWith("[Net]")){
                try{
                    String music_url=message.send.replace("[Net]","");
                    Mp3Player mp3Player=new Mp3Player(music_url);
                    if(musicThread!=null){
                        if(musicThread.isAlive()){
                            musicThread.stop();
                        }
                        musicThread=null;
                    }
                    musicThread=new Thread(mp3Player);
                    musicThread.start();
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }else {
                //stop music
                if(musicThread!=null){
                    if(musicThread.isAlive()){
                        musicThread.stop();
                        musicThread=null;
                    }
                }
            }
        }
        return null;
    }
}
