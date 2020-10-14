package team.info.ncmfm.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.audio.Mp3Player;

public class MusicMessageClientHandler implements IMessageHandler<MusicMessage, IMessage> {
    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if(ctx.side.isClient()){
            if(message.send.startsWith("[Net]")){
                try{
                    String music_url=message.send.replace("[Net]","");
                    Mp3Player mp3Player=new Mp3Player(music_url);
                    if(NcmMod.musicThread!=null){
                        if(NcmMod.musicThread.isAlive()){
                            NcmMod.musicThread.stop();
                        }
                        NcmMod.musicThread=null;
                    }
                    NcmMod.musicThread=new Thread(mp3Player);
                    NcmMod.musicThread.start();
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }else {
                //stop music
                if(NcmMod.musicThread!=null){
                    if(NcmMod.musicThread.isAlive()){
                        NcmMod.musicThread.stop();
                        NcmMod.musicThread=null;
                    }
                }
            }
        }
        return null;
    }
}
