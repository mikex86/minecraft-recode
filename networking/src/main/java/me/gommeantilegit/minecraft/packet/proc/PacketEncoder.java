package me.gommeantilegit.minecraft.packet.proc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.packet.packets.server.ServerDisconnectMessagePacket;
import me.gommeantilegit.minecraft.utils.serialization.Serializer;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;

/**
 * @param <T> the type of packet to be encoded
 */
public abstract class PacketEncoder<T extends Packet> extends MessageToByteEncoder<T> implements Serializer<T> {

    /**
     * Invoked by Netty to encode every sent message into a byte buffer
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
     * @param msg the message to encode
     * @param out the {@link ByteBuf} into which the encoded message will be written
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, T msg, ByteBuf out) {
        try {
            BitByteBuffer buffer = new BitByteBuffer(out);
            serialize(msg, buffer);
            out.writeBytes(buffer.retrieveBytes());
        } catch (Throwable e) {
            ctx.channel().writeAndFlush(new ServerDisconnectMessagePacket(null, "Internal Exception caught!"));
            System.out.println("PacketEncodingException caught! Closing channel!");
            e.printStackTrace();
            ctx.close().syncUninterruptibly();
        }
    }
}
