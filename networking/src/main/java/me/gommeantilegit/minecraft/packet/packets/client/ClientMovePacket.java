package me.gommeantilegit.minecraft.packet.packets.client;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A packet sent by the client when it has moved
 */
@PacketInfo(side = PacketInfo.PacketSide.CLIENT, encoder = ClientMovePacket.Encoder.class, decoder = ClientMovePacket.Decoder.class)
public class ClientMovePacket extends ClientPacket {

    @PacketID
    public static final short PACKET_ID = CLIENT_PACKET_BIT | 6;


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


    public ClientMovePacket(@Nullable Channel clientChannel, @Nullable Vector3 position, @Nullable Vector2 rotation) {
        super(PACKET_ID, clientChannel);
        this.position = position;
        this.rotation = rotation;
    }

    @Nullable
    public Vector3 getPosition() {
        return position;
    }

    @Nullable
    public Vector2 getRotation() {
        return rotation;
    }

    public boolean hasPosition() {
        return position != null;
    }

    public boolean hasRotation() {
        return rotation != null;
    }

    public static class Encoder extends PacketEncoder<ClientMovePacket> {

        @Override
        public void serialize(@NotNull ClientMovePacket packet, @NotNull BitByteBuffer buf) {
            byte flagByte = 0;
            if (packet.hasPosition())
                flagByte |= 0b00000001;
            if (packet.hasRotation())
                flagByte |= 0b00000010;
            buf.writeByte(flagByte);
            if (packet.hasPosition())
                buf.writeVector3(Objects.requireNonNull(packet.getPosition()));
            if (packet.hasRotation())
                buf.writeVector2(Objects.requireNonNull(packet.getRotation()));
        }
    }

    public static class Decoder extends PacketDecoder<ClientMovePacket> {

        @Nullable
        @Override
        public ClientMovePacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
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
            return new ClientMovePacket(channel, position, rotation);
        }
    }

}
