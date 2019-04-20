package me.gommeantilegit.minecraft.packet.handler;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract implementation of {@link IPacketListener} storing the class of T and a parent channel (nullable)
 *
 * @param <T> the type of packet
 */
public abstract class AbstractPacketListener<T extends Packet> implements IPacketListener<T> {

    /**
     * Class of packet type
     */
    @NotNull
    private final Class<T> packetClass;

    /**
     * Channel that the listener should only be invoked with. If null -> invoked with all channels
     */
    @Nullable
    private final Channel channel;

    protected AbstractPacketListener(@NotNull Class<T> packetClass, @Nullable Channel channel) {
        this.packetClass = packetClass;
        this.channel = channel;
    }

    @Nullable
    public Channel getChannel() {
        return channel;
    }

    @NotNull
    public Class<T> getPacketClass() {
        return packetClass;
    }
}
