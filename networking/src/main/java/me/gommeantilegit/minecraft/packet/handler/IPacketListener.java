package me.gommeantilegit.minecraft.packet.handler;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.Packet;
import org.jetbrains.annotations.NotNull;
/**
 * Represents a listener invoked on receive of a packet type of T
 * @param <T> the type of packet that is received
 */
public interface IPacketListener<T extends Packet> {

    /**
     * Called if the expected packet is received
     *
     * @param packet  the packet that was received
     * @param channel the channel that sent it
     */
    void onPacketReceived(@NotNull T packet, @NotNull Channel channel);
}
