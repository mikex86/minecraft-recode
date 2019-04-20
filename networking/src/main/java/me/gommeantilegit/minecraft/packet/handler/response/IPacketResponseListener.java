package me.gommeantilegit.minecraft.packet.handler.response;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.Packet;
import me.gommeantilegit.minecraft.packet.handler.IPacketListener;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a listener that waits for a response packet
 *
 * @param <T> type of the packet response that is expected to be received
 */
public interface IPacketResponseListener<T extends Packet> extends IPacketListener<T> {

    /**
     * Called if the Packet was not sent in the specified timeout
     *
     * @param channel the channel that did not sent the expected packet
     */
    void onTimeOut(@NotNull Channel channel);
}
