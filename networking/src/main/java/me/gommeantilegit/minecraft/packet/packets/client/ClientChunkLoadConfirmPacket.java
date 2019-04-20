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

@PacketInfo(side = PacketInfo.PacketSide.CLIENT, encoder = ClientChunkLoadConfirmPacket.Encoder.class, decoder = ClientChunkLoadConfirmPacket.Decoder.class)
public class ClientChunkLoadConfirmPacket extends ClientPacket {

    /**
     * PacketID
     */
    @PacketID
    public static final short PACKET_ID = CLIENT_PACKET_BIT | 4;

    /**
     * Chunk Origin vector of the chunk that has been loaded by the client
     */
    @NotNull
    private final Vector2 chunkOrigin;

    /**
     * @param clientChannel the client that sent the packet
     * @param chunkOrigin   the chunk origin of the chunk that has benn loaded by the client
     */
    public ClientChunkLoadConfirmPacket(@Nullable Channel clientChannel, @NotNull Vector2 chunkOrigin) {
        super(PACKET_ID, clientChannel);
        this.chunkOrigin = chunkOrigin;
    }

    @NotNull
    public Vector2 getChunkOrigin() {
        return chunkOrigin;
    }


    public static class Encoder extends PacketEncoder<ClientChunkLoadConfirmPacket> {

        @Override
        public void serialize(@NotNull ClientChunkLoadConfirmPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeVector2(packet.getChunkOrigin());
        }
    }

    public static class Decoder extends PacketDecoder<ClientChunkLoadConfirmPacket> {

        @Nullable
        @Override
        public ClientChunkLoadConfirmPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ClientChunkLoadConfirmPacket(channel, buffer.readVector2());
        }
    }
}
