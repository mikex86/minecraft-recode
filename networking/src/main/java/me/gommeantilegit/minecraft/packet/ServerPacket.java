package me.gommeantilegit.minecraft.packet;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet that is sent by the server
 */
public class ServerPacket extends Packet {

    /**
     * Short Most Significant Bit is 0 as server packet indicator
     */
    public static final short SERVER_PACKET_BIT = 0b000_0000_0000_0000;

    /**
     * @param packetID the id of the packet
     * @param serverChannel the server channel that sent this packet
     * @param gamePacket sets {@link #gamePacket}
     */
    public ServerPacket(short packetID, @Nullable Channel serverChannel, boolean gamePacket) {
        super(packetID, serverChannel, gamePacket);
    }

    public ServerPacket(short packetID, @Nullable Channel serverChannel) {
        this(packetID, serverChannel, true);
    }

}
