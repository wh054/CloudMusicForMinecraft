package team.info.ncmfm.net;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import paulscode.sound.SoundSystemConfig;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.model.MusicInfoWrapper;

import java.net.MalformedURLException;
import java.net.URL;

public class MusicMessageClientHandler implements IMessageHandler<MusicMessage, IMessage> {
    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            MusicInfoWrapper packet=new Gson().fromJson(message.send,MusicInfoWrapper.class);

            if(packet.getCommand().equals(EnumMusicCommand.PLAY)){
                if(packet.getPos()!=null){
                    try {
                        NcmMod.soundSystem.newStreamingSource(
                                false,
                                "custom.mp3",
                                new URL(packet.getSource()),
                                "custom.mp3",
                                true,
                                packet.getPos().getX(),
                                packet.getPos().getY(),
                                packet.getPos().getZ(),
                                SoundSystemConfig.ATTENUATION_LINEAR,
                                SoundSystemConfig.getDefaultFadeDistance()
                        );
                        NcmMod.soundSystem.play("custom.mp3");
                        System.out.println(packet.getCommand());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }else {
                    Minecraft.getMinecraft().getSoundHandler().stopSounds();
                    NcmMod.soundSystem.backgroundMusic("custom.mp3", packet.getSource(), true);
                }
            }else {
                NcmMod.soundSystem.stop("custom.mp3");
            }
        }
        return null;
    }
}