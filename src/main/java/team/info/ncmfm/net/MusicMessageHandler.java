package team.info.ncmfm.net;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MusicMessageHandler implements IMessageHandler<MusicMessage, IMessage> {

    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if(ctx.side.isServer()){
            MusicPacketHandler.INSTANCE.sendToAll(new MusicMessage(message.send));
        }
        return null;
    }
}
