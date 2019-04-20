package me.gommeantilegit.minecraft.packet.handler.response;

import me.gommeantilegit.minecraft.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.Channel;

/**
 * Abstract implementation of {@link IPacketResponseListener} that stores the channel that is expected to send a packet
 * @param <T> the type of packet that is expected
 */
public abstract class AbstractPacketResponseListener<T extends Packet> implements IPacketResponseListener<T> {

    /**
     * The channel that is expected to send the packet
     */
    @NotNull
    private final Channel channel;

    /**
     * @param channel sets {@link #channel}
     */
    public AbstractPacketResponseListener(@NotNull Channel channel) {
        this.channel = channel;
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }
}
