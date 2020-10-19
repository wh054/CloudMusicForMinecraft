package team.info.ncmfm.net;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.info.ncmfm.NcmMod;

public class MusicMessageClientHandler implements IMessageHandler<MusicMessage, IMessage> {
    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            if (message.send.startsWith("[Net]")) {
                String music_url = message.send.replace("[Net]", "");
                Minecraft.getMinecraft().getSoundHandler().stopSounds();
                NcmMod.soundSystem.backgroundMusic("custom.mp3", music_url, true);
            }else {
                NcmMod.soundSystem.stop("custom.mp3");
            }
        }
        return null;
    }
}