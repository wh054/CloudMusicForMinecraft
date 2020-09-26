package team.info.ncmfm.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MusicMessage implements IMessage {

    public String send;

    public MusicMessage() {
    }

    public MusicMessage(String send){
        this.send=send;
    }


    //读取
    @Override
    public void fromBytes(ByteBuf buf) {
        send = convertByteBufToString(buf);
    }


    //写入
    @Override
    public void toBytes(ByteBuf buf) {
        stringToByteBuf(buf,send);
    }


    private String convertByteBufToString(ByteBuf buf) {
        String str;
        if (buf.hasArray()) { // 处理堆缓冲区
            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else { // 处理直接缓冲区以及复合缓冲区
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            str = new String(bytes, 0, buf.readableBytes());
        }
        return str;
    }

    private void stringToByteBuf(ByteBuf buf,String str){
        byte[] bytes = str.getBytes();
        buf.writeBytes(bytes);
    }
}
