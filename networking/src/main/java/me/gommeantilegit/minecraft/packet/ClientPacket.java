package me.gommeantilegit.minecraft.packet;

import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet sent by the client
 */
public class ClientPacket extends Packet {

    /**
     * Short Most Significant Bit is 1 as client packet indicator
     */
    public static final short CLIENT_PACKET_BIT = 0b100_0000_0000_0000;

    /**
     * @param packetID      id of the packet
     * @param clientChannel the client that sent the packet. See {@link #channel}
     */
    public ClientPacket(short packetID, @Nullable Channel clientChannel) {
        super(packetID, clientChannel);
    }

    /**
     * @param packetID   id of the packet
     * @param channel    the client that sent the packet. See {@link #channel}
     * @param gamePacket state if te packet is a game packet. See {@link #gamePacket}
     */
    public ClientPacket(short packetID, @Nullable Channel channel, boolean gamePacket) {
        super(packetID, channel, gamePacket);
    }

}
