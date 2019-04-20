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

@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerRequestUserInfoPacket.Encoder.class, decoder = ServerRequestUserInfoPacket.Decoder.class)
public class ServerRequestUserInfoPacket extends ServerPacket {

    /**
     * Packet ID
     */
    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 1;

    /**
     * @param serverChannel the server channel that sent this packet
     */
    public ServerRequestUserInfoPacket(@Nullable Channel serverChannel) {
        super(PACKET_ID, serverChannel, false);
    }

    public static class Decoder extends PacketDecoder<ServerRequestUserInfoPacket> {

        @Nullable
        @Override
        public ServerRequestUserInfoPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            return new ServerRequestUserInfoPacket(channel);
        }
    }

    public static class Encoder extends PacketEncoder<ServerRequestUserInfoPacket> {

        @Override
        public void serialize(@NotNull ServerRequestUserInfoPacket packet, @NotNull BitByteBuffer buf) {

        }
    }
}
