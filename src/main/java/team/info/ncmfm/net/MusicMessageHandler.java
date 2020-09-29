package team.info.ncmfm.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import team.info.ncmfm.NcmMod;

public class MusicMessageHandler implements IMessageHandler<MusicMessage, IMessage> {

    @Override
    public IMessage onMessage(MusicMessage message, MessageContext ctx) {
        if(ctx.side.isServer()){
            String musicUrl = message.send;
            System.out.println(musicUrl);
            if(NcmMod.AudioBufferChannel!=null){
                NcmMod.AudioBufferChannel.sendToAll(createFMLProxyPacket("[Stop]"));
                NcmMod.AudioBufferChannel.sendToAll(createFMLProxyPacket("[Net]"+musicUrl));
            }
        }
        return null;
    }

    private FMLProxyPacket createFMLProxyPacket(String msg) {
        ByteBuf buffer = Unpooled.buffer();
        stringToByteBuf(buffer,msg);
        return new FMLProxyPacket(new PacketBuffer(buffer), "AudioBuffer");
    }

    private void stringToByteBuf(ByteBuf buf,String str){
        byte[] bytes = str.getBytes();
        buf.writeBytes(bytes);
    }
}
