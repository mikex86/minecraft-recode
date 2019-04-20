package me.gommeantilegit.minecraft.packet.packets.server;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Packet sent by the server to modify a players position and/or rotation
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerPositionSetPacket.Encoder.class, decoder = ServerPositionSetPacket.Decoder.class)
public class ServerPositionSetPacket extends ServerPacket {

    /**
     * Packet ID
     */
    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 5;

    /**
     * Position
     */
    @Nullable
    private final Vector3 position;

    /**
     * Rotation [x = yaw, y = pitch]
     */
    @Nullable
    private final Vector2 rotation;

    /**
     * @param serverChannel the server channel that sent this packet
     * @param position      player position (null if not needed)
     * @param rotation      player rotation (null if not needed)
     */
    public ServerPositionSetPacket(@Nullable Channel serverChannel, @Nullable Vector3 position, @Nullable Vector2 rotation) {
        super(PACKET_ID, serverChannel);
        this.position = position;
        this.rotation = rotation;
    }

    @Nullable
    public Vector2 getRotation() {
        return rotation;
    }

    @Nullable
    public Vector3 getPosition() {
        return position;
    }

    public boolean hasPosition() {
        return position != null;
    }

    public boolean hasRotation() {
        return rotation != null;
    }

    public static class Encoder extends PacketEncoder<ServerPositionSetPacket> {

        @Override
        public void serialize(@NotNull ServerPositionSetPacket packet, @NotNull BitByteBuffer buf) {
            boolean hasPosition = packet.hasPosition(), hasRotation = packet.hasRotation();
            byte flag = 0;
            if (hasPosition)
                flag |= 0b00000001;
            if (hasRotation)
                flag |= 0b00000010;
            buf.writeByte(flag);
            if (hasPosition)
                buf.writeVector3(Objects.requireNonNull(packet.getPosition()));
            if (hasRotation)
                buf.writeVector2(Objects.requireNonNull(packet.getRotation()));
        }
    }

    public static class Decoder extends PacketDecoder<ServerPositionSetPacket> {

        @Nullable
        @Override
        public ServerPositionSetPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            byte flag = buffer.readByte();
            byte positionBit = 0b00000001;
            byte rotationBit = 0b00000010;
            Vector3 position = null;
            Vector2 rotation = null;
            if ((flag & positionBit) != 0) {
                position = buffer.readVector3();
            }
            if ((flag & rotationBit) != 0) {
                rotation = buffer.readVector2();
            }
            return new ServerPositionSetPacket(channel, position, rotation);
        }
    }
}
