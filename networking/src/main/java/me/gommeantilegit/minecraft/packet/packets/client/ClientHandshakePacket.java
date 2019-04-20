package me.gommeantilegit.minecraft.packet.packets.client;

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
 * Packet to perform connection handshake (First validation of the connection being a real minecraft client)
 */
@PacketInfo(side = PacketInfo.PacketSide.CLIENT, decoder = ClientHandshakePacket.Decoder.class, encoder = ClientHandshakePacket.Encoder.class)
public class ClientHandshakePacket extends ClientPacket {

    /**
     * Packet ID
     */
    @PacketID
    public static final short PACKET_ID = CLIENT_PACKET_BIT | 1;

    public ClientHandshakePacket(@Nullable Channel channel) {
        super(PACKET_ID, channel, false);
    }

    /**
     * Packet decoder for {@link ClientHandshakePacket}
     */
    public static class Decoder extends PacketDecoder<ClientHandshakePacket> {

        @Nullable
        @Override
        public ClientHandshakePacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ClientHandshakePacket(channel);
        }
    }

    /**
     * Packet encoder for {@link ClientHandshakePacket}
     */
    public static class Encoder extends PacketEncoder<ClientHandshakePacket> {

        @Override
        public void serialize(@NotNull ClientHandshakePacket packet, @NotNull BitByteBuffer buf) {
        }
    }
}
