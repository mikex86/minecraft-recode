package me.gommeantilegit.minecraft.packet.packets.server;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.SerializationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerDisconnectMessagePacket.Encoder.class, decoder = ServerDisconnectMessagePacket.Decoder.class)
public class ServerDisconnectMessagePacket extends ServerPacket {

    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 9;

    @NotNull
    private final String message;

    public ServerDisconnectMessagePacket(@Nullable Channel serverChannel, @NotNull String message) {
        super(PACKET_ID, serverChannel, false);
        this.message = message;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public static final class Encoder extends PacketEncoder<ServerDisconnectMessagePacket> {

        @Override
        public void serialize(@NotNull ServerDisconnectMessagePacket object, @NotNull BitByteBuffer buf) throws SerializationException {
            buf.writeString(object.getMessage());
        }

    }

    public static final class Decoder extends PacketDecoder<ServerDisconnectMessagePacket> {

        @Override
        public ServerDisconnectMessagePacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) throws PacketDecodingException {
            return new ServerDisconnectMessagePacket(channel, buffer.readString());
        }
    }
}
