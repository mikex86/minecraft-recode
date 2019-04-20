package me.gommeantilegit.minecraft.packet.packets.server;

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
 * Packet sent by the server to indicate successful session establishment
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerSessionValidationConfirmationPacket.Encoder.class, decoder = ServerSessionValidationConfirmationPacket.Decoder.class)
public class ServerSessionValidationConfirmationPacket extends ServerPacket {

    /**
     * PacketID
     */
    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 2;

    /**
     * @param serverChannel the server channel that sent this packet
     */
    public ServerSessionValidationConfirmationPacket(@Nullable Channel serverChannel) {
        super(PACKET_ID, serverChannel, false);
    }

    public static class Encoder extends PacketEncoder<ServerSessionValidationConfirmationPacket> {

        @Override
        public void serialize(@NotNull ServerSessionValidationConfirmationPacket packet, @NotNull BitByteBuffer buf) {
        }
    }

    public static class Decoder extends PacketDecoder<ServerSessionValidationConfirmationPacket> {

        @Nullable
        @Override
        public ServerSessionValidationConfirmationPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ServerSessionValidationConfirmationPacket(channel);
        }
    }

}
