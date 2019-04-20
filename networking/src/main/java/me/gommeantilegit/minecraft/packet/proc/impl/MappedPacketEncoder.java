package me.gommeantilegit.minecraft.packet.proc.impl;

import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.utils.serialization.exception.SerializationException;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;

public abstract class MappedPacketEncoder<T extends Packet> extends PacketEncoder<T> {

    /**
     * Stores the parent packet decoders for the given packetID
     */
    @NotNull
    private final HashMap<Short, PacketEncoder<T>> encoderMap = new HashMap<>();

    public MappedPacketEncoder() {
        registerPacketEncoders();
        System.out.println();
    }

    /**
     * Called on object creation to register all packet encoders
     */
    protected abstract void registerPacketEncoders();

    /**
     * @param id      the id of the packet that the specified encodes encodes
     * @param decoder the encoder to encode the packet
     */
    public void registerPacketEncoder(short id, @NotNull PacketEncoder<T> decoder) {
        this.encoderMap.put(id, decoder);
    }

    @Override
    public void serialize(@NotNull T packet, @NotNull BitByteBuffer buf) throws SerializationException {
        short id = packet.getPacketID();
        buf.writeShort(id); // Writing packet id to the packet buffer
        Optional.ofNullable(this.encoderMap.get(id)).orElseThrow(() -> new SerializationException("Unknown packet id: " + id + "!")).serialize(packet, buf);
    }
}
