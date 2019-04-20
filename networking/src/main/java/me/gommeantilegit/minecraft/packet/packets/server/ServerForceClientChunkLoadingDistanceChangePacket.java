package me.gommeantilegit.minecraft.packet.packets.server;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A packet sent to the client to change it's render / chunk loading distance.
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerForceClientChunkLoadingDistanceChangePacket.Encoder.class, decoder = ServerForceClientChunkLoadingDistanceChangePacket.Decoder.class)
public class ServerForceClientChunkLoadingDistanceChangePacket extends ServerPacket {

    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 7;

    /**
     * The render distance that the client should adapt
     */
    private final int renderDistance;

    public ServerForceClientChunkLoadingDistanceChangePacket(@Nullable Channel serverChannel, int renderDistance) {
        super(PACKET_ID, serverChannel, false);
        this.renderDistance = renderDistance;
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public static final class Encoder extends PacketEncoder<ServerForceClientChunkLoadingDistanceChangePacket> {

        @Override
        public void serialize(@NotNull ServerForceClientChunkLoadingDistanceChangePacket object, @NotNull BitByteBuffer buf) {
            buf.writeInt(object.getRenderDistance());
        }

    }

    public static final class Decoder extends PacketDecoder<ServerForceClientChunkLoadingDistanceChangePacket> {

        @Override
        public ServerForceClientChunkLoadingDistanceChangePacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ServerForceClientChunkLoadingDistanceChangePacket(channel, buffer.readInt());
        }
    }
}
