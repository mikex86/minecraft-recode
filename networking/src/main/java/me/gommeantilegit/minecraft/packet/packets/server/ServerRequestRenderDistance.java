package me.gommeantilegit.minecraft.packet.packets.server;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.packet.packets.client.ClientChunkLoadingDistanceChangePacket;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Packet sent to request a {@link ClientChunkLoadingDistanceChangePacket}
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerRequestRenderDistance.Encoder.class, decoder = ServerRequestRenderDistance.Decoder.class)
public class ServerRequestRenderDistance extends ServerPacket {

    /**
     * Packet ID
     */
    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 8;

    /**
     * @param serverChannel the server channel that sent this packet
     */
    public ServerRequestRenderDistance(@Nullable Channel serverChannel) {
        super(PACKET_ID, serverChannel, false);
    }

    public static class Decoder extends PacketDecoder<ServerRequestRenderDistance> {

        @Nullable
        @Override
        public ServerRequestRenderDistance deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ServerRequestRenderDistance(channel);
        }
    }

    public static class Encoder extends PacketEncoder<ServerRequestRenderDistance> {

        @Override
        public void serialize(@NotNull ServerRequestRenderDistance packet, @NotNull BitByteBuffer buf) {

        }
    }
}
