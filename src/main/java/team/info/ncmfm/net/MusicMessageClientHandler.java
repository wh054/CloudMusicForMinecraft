package team.info.ncmfm.net;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import paulscode.sound.SoundSystemConfig;
import team.info.ncmfm.model.MusicInfoWrapper;
import team.info.ncmfm.proxy.ClientProxy;
import team.info.ncmfm.utils.EncryptUtil;
import java.net.URL;

public class MusicMessageClientHandler implements IMessageHandler<MusicMessage, IMessage> {
    private static final Logger logger = LogManager.getLogger(MusicMessageClientHandler.class);

    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            MusicInfoWrapper packet=new Gson().fromJson(message.send,MusicInfoWrapper.class);

            if(packet.getCommand().equals(EnumMusicCommand.PLAY)){
                if(packet.getPos()!=null){
                    String id=EncryptUtil.MD5(packet.getPos().toString())+".MonoMp3";
                    try {
                        ClientProxy.soundSystem.newStreamingSource(
                                false,
                                id,
                                new URL(packet.getSource()),
                                id,
                                true,
                                packet.getPos().getX(),
                                packet.getPos().getY(),
                                packet.getPos().getZ(),
                                SoundSystemConfig.ATTENUATION_ROLLOFF,
                                0.15f
                        );
                        ClientProxy.soundSystem.setVolume(id,1f);
                        ClientProxy.soundSystem.play(id);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }else {
                    Minecraft.getMinecraft().getSoundHandler().stopSounds();
                    try {
                        ClientProxy.soundSystem.backgroundMusic(
                                "background.StereoMp3",
                               new URL(packet.getSource()),
                                "background.StereoMp3",
                                true);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }else {
                if(packet.getPos()!=null){
                    String id=EncryptUtil.MD5(packet.getPos().toString())+".MonoMp3";
                    ClientProxy.soundSystem.stop(id);
                }else{
                    ClientProxy.soundSystem.stop("background.StereoMp3");
                }
            }
        }
        return null;
    }
}