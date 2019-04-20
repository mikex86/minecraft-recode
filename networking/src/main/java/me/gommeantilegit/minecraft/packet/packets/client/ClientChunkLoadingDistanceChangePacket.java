package me.gommeantilegit.minecraft.packet.packets.client;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Packet telling the server the client's chunk loading / render distance
 */
@PacketInfo(side = PacketInfo.PacketSide.CLIENT, decoder = ClientChunkLoadingDistanceChangePacket.Decoder.class, encoder = ClientChunkLoadingDistanceChangePacket.Encoder.class)
public class ClientChunkLoadingDistanceChangePacket extends ClientPacket {

    @PacketID
    public static final short PACKET_ID = CLIENT_PACKET_BIT | 7;

    /**
     * The render distance of the client
     */
    private final int renderDistance;

    public ClientChunkLoadingDistanceChangePacket(@Nullable Channel clientChannel, int renderDistance) {
        super(PACKET_ID, clientChannel);
        this.renderDistance = renderDistance;
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public static final class Encoder extends PacketEncoder<ClientChunkLoadingDistanceChangePacket> {

        @Override
        public void serialize(@NotNull ClientChunkLoadingDistanceChangePacket object, @NotNull BitByteBuffer buf) {
            buf.writeInt(object.getRenderDistance());
        }

    }

    public static final class Decoder extends PacketDecoder<ClientChunkLoadingDistanceChangePacket>{

        @Override
        public ClientChunkLoadingDistanceChangePacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ClientChunkLoadingDistanceChangePacket(channel, buffer.readInt());
        }
    }

}
