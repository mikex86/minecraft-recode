package me.gommeantilegit.minecraft.packet.proc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Decoder object for a given packet type
 *
 * @param <T> the type of packet to be decoded
 */
public abstract class PacketDecoder<T extends Packet> extends ByteToMessageDecoder {

    /**
     * Constructs a packet from the specified ByteBuffer
     *
     * @param buffer  the buffer that the packet should be constructed from
     * @param channel the channel that sent this packet buffer
     * @return the newly constructed packet
     */
    @Nullable
    public abstract T deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) throws PacketDecodingException;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            T t = deserialize(new BitByteBuffer(in), ctx.channel());
            if (t != null)
                out.add(t);
        } catch (Throwable e) {
            //TODO: ERROR MESSAGE
            System.out.println("PacketDecodingException caught! Closing Channel!");
            ctx.close().syncUninterruptibly();
            e.printStackTrace();
        }
    }
}
