package me.gommeantilegit.minecraft.packet;

import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet that is sent over the client/server connection
 */
public abstract class Packet {

    /**
     * Packet packetID
     */
    private final short packetID;

    /**
     * State if the packet is a game packet - meaning it is used to control the game
     */
    private final boolean gamePacket;

    /**
     * The channel that sent this packet
     * Null, if the packet is on its native side (eg ClientPacket on side Client) (eg sent packet instance)
     */
    @Nullable
    private final Channel channel;

    public Packet(short packetID, @Nullable Channel channel) {
        this(packetID, channel, true);
    }

    /**
     * @param packetID the packetID of the packet
     * @param channel channel that sent the packet. See {@link #channel}
     * @param gamePacket state if the packet is a game packet
     */
    public Packet(short packetID, @Nullable Channel channel, boolean gamePacket) {
        this.packetID = packetID;
        this.channel = channel;
        this.gamePacket = gamePacket;
    }

    public Channel getChannel() {
        return channel;
    }

    public short getPacketID() {
        return packetID;
    }

    public boolean isGamePacket() {
        return gamePacket;
    }
}
