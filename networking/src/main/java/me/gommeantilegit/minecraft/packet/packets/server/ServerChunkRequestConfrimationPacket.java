package me.gommeantilegit.minecraft.packet.packets.server;

import com.badlogic.gdx.math.Vector2;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Packet sent by the server to indicate, that a {@link me.gommeantilegit.minecraft.packet.packets.client.ClientRequestChunkDataPacket} has been received
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerChunkRequestConfrimationPacket.Encoder.class, decoder = ServerChunkRequestConfrimationPacket.Decoder.class)
public class ServerChunkRequestConfrimationPacket extends ServerPacket {

    /**
     * Packet ID
     */
    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 6;

    /**
     * The chunk origin of the chunk which data request was confirmed
     */
    @NotNull
    private final Vector2 chunkOrigin;

    public ServerChunkRequestConfrimationPacket(@Nullable Channel serverChannel, @NotNull Vector2 chunkOrigin) {
        super(PACKET_ID, serverChannel);
        this.chunkOrigin = chunkOrigin;
    }

    @NotNull
    public Vector2 getChunkOrigin() {
        return chunkOrigin;
    }

    public static class Encoder extends PacketEncoder<ServerChunkRequestConfrimationPacket> {

        @Override
        public void serialize(@NotNull ServerChunkRequestConfrimationPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeVector2(packet.getChunkOrigin());
        }
    }

    public static class Decoder extends PacketDecoder<ServerChunkRequestConfrimationPacket> {

        @Nullable
        @Override
        public ServerChunkRequestConfrimationPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ServerChunkRequestConfrimationPacket(channel, buffer.readVector2());
        }
    }
}
