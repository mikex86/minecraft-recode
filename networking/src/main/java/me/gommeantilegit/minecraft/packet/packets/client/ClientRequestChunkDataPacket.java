package me.gommeantilegit.minecraft.packet.packets.client;

import com.badlogic.gdx.math.Vector2;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Packet sent by the client to request the server to send information about a given chunk
 */
@PacketInfo(side = PacketInfo.PacketSide.CLIENT, encoder = ClientRequestChunkDataPacket.Encoder.class, decoder = ClientRequestChunkDataPacket.Decoder.class)
public class ClientRequestChunkDataPacket extends ClientPacket {

    /**
     * PacketID
     */
    @PacketID
    public static final short PACKET_ID = CLIENT_PACKET_BIT | 3;

    /**
     * The Origin of the chunk that is requested
     */
    @NotNull
    private final Vector2 chunkOrigin;

    /**
     * @param clientChannel the client that sent the packet
     * @param chunkOrigin   the chunk origin of the chunk that is requested
     */
    public ClientRequestChunkDataPacket(@Nullable Channel clientChannel, @NotNull Vector2 chunkOrigin) {
        super(PACKET_ID, clientChannel);
        this.chunkOrigin = chunkOrigin;
    }

    @NotNull
    public Vector2 getChunkOrigin() {
        return chunkOrigin;
    }

    public static class Encoder extends PacketEncoder<ClientRequestChunkDataPacket> {

        @Override
        public void serialize(@NotNull ClientRequestChunkDataPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeVector2(packet.getChunkOrigin());
        }
    }

    public static class Decoder extends PacketDecoder<ClientRequestChunkDataPacket> {

        @Nullable
        @Override
        public ClientRequestChunkDataPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ClientRequestChunkDataPacket(channel, buffer.readVector2());
        }
    }
}
