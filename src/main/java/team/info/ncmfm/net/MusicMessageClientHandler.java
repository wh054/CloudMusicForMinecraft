package team.info.ncmfm.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.audio.PlayerThreadWrapper;

public class MusicMessageClientHandler implements IMessageHandler<MusicMessage, IMessage> {
    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if(ctx.side.isClient()){
            if(message.send.startsWith("[Net]")){
                try{
                    if(NcmMod.mp3Player!=null){
                        NcmMod.mp3Player.close();
                    }
                    String music_url=message.send.replace("[Net]","");
                    PlayerThreadWrapper threadWrapper=new PlayerThreadWrapper(music_url);
                    Thread thread=new Thread(threadWrapper);
                    thread.start();
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }else {
                if(NcmMod.mp3Player!=null){
                    NcmMod.mp3Player.close();
                }
            }
        }
        return null;
    }
}
