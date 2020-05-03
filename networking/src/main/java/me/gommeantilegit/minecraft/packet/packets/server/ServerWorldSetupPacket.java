package me.gommeantilegit.minecraft.packet.packets.server;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Packet containing world meta data
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, decoder = ServerWorldSetupPacket.Decoder.class, encoder = ServerWorldSetupPacket.Encoder.class)
public class ServerWorldSetupPacket extends ServerPacket {

    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 3;

    /**
     * Stores the height of the world
     */
    private final int worldHeight;

    /**
     * The world time of the world at {@link #packetSentUnixTime}
     */
    private final long worldTime;

    /**
     * The unix time stamp when the packet was sent
     */
    private final long packetSentUnixTime;

    /**
     * @param serverChannel server channel. See {@link #channel}
     * @param world         the world that should be setup of the client
     */
    @SideOnly(side = Side.SERVER)
    public ServerWorldSetupPacket(@Nullable Channel serverChannel, @NotNull WorldBase world) {
        super(PACKET_ID, serverChannel, false);
        this.worldHeight = world.getHeight();
        this.worldTime = world.worldTime;
        this.packetSentUnixTime = new Date().getTime();
    }

    /**
     * Client de-serialization constructor
     *
     * @param serverChannel      the server channel. See {@link #channel}
     * @param worldHeight        See {@link #worldHeight}
     * @param worldTime          See {@link #worldTime}
     * @param packetSentUnixTime See {@link #packetSentUnixTime}
     */
    @SideOnly(side = Side.CLIENT)
    public ServerWorldSetupPacket(@NotNull Channel serverChannel, int worldHeight, long worldTime, long packetSentUnixTime) {
        super(PACKET_ID, serverChannel, false);
        this.worldHeight = worldHeight;
        this.worldTime = worldTime;
        this.packetSentUnixTime = packetSentUnixTime;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public long getPacketSentUnixTime() {
        return packetSentUnixTime;
    }

    public long getWorldTime() {
        return worldTime;
    }

    public static class Encoder extends PacketEncoder<ServerWorldSetupPacket> {

        @Override
        public void serialize(@NotNull ServerWorldSetupPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeInt(packet.worldHeight);
            buf.writeLong(packet.worldTime);
            buf.writeLong(new Date().getTime());
        }
    }

    public static class Decoder extends PacketDecoder<ServerWorldSetupPacket> {

        @Nullable
        @Override
        public ServerWorldSetupPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ServerWorldSetupPacket(channel, buffer.readInt(), buffer.readLong(), buffer.readLong());
        }
    }
}
