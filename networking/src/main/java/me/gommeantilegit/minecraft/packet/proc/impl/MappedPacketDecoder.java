package me.gommeantilegit.minecraft.packet.proc.impl;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

public abstract class MappedPacketDecoder<T extends Packet> extends PacketDecoder<T> {

    /**
     * Stores the parent packet decoders for the given packetID
     */
    @NotNull
    private final HashMap<Short, PacketDecoder<? extends T>> decoderMap = new HashMap<>();

    /**
     * Stores the parent minecraft instance
     */
    @NotNull
    private final AbstractMinecraft mc;

    public MappedPacketDecoder(@NotNull AbstractMinecraft mc) {
        this.mc = mc;
        registerPacketDecoders();
    }

    /**
     * Called on object creation to register all packet decoders
     */
    protected abstract void registerPacketDecoders();

    /**
     * @param id      the id of the packet that the specified decoder decodes
     * @param decoder the decoder to decode the packet
     */
    public void registerPacketDecoder(short id, @NotNull PacketDecoder<? extends T> decoder) {
        this.decoderMap.put(id, decoder);
    }

    @Override
    public T deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) throws PacketDecodingException {
        if (buffer.bytes() >= 2) {
            short id = buffer.readShort();
            if (id > 0)
                return Optional.ofNullable(this.decoderMap.get(id)).orElseThrow(() -> new PacketDecodingException("Unknown packet id: " + id + "!")).deserialize(buffer, channel);
            else return null;
        } else {
            return null;
        }
    }
}
