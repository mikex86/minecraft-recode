package me.gommeantilegit.minecraft.packet.packets.client;

import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinBase;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinPixel;
import me.gommeantilegit.minecraft.packet.ClientPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@PacketInfo(side = PacketInfo.PacketSide.CLIENT, decoder = ClientUserInfoPacket.Decoder.class, encoder = ClientUserInfoPacket.Encoder.class)
public class ClientUserInfoPacket extends ClientPacket {

    /**
     * Packet ID
     */
    @PacketID
    public static final short PACKET_ID = CLIENT_PACKET_BIT | 2;

    /**
     * User name of the player
     */
    @NotNull
    private final String userName;

    /**
     * The skin of the player (player model texture)
     */
    @NotNull
    private final SkinBase skin;

    /**
     * @param clientChannel the client that sent the packet
     * @param userName      sets {@link #userName}
     * @param skin          sets {@link #skin}
     */
    public ClientUserInfoPacket(@Nullable Channel clientChannel, @NotNull String userName, @NotNull SkinBase skin) {
        super(PACKET_ID, clientChannel, false);
        this.userName = userName;
        this.skin = skin;
    }

    @NotNull
    public SkinBase getSkin() {
        return skin;
    }

    @NotNull
    public String getUserName() {
        return userName;
    }

    public static class Decoder extends PacketDecoder<ClientUserInfoPacket> {

        @Nullable
        @Override
        public ClientUserInfoPacket deserialize(@NotNull BitByteBuffer buffer, @NotNull Channel channel) {
            String userName = buffer.readString();
            int width = buffer.readInt();
            int height = buffer.readInt();
            SkinPixel[][] pixels = new SkinPixel[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pixels[x][y] = new SkinPixel(buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readByte());
                }
            }
            return new ClientUserInfoPacket(channel, userName, new SkinBase(pixels));
        }
    }

    public static class Encoder extends PacketEncoder<ClientUserInfoPacket> {

        @Override
        public void serialize(@NotNull ClientUserInfoPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeString(packet.getUserName()); // Username
            SkinPixel[][] pixels = packet.getSkin().getPixels();
            int width = pixels.length, height = pixels[0].length; // Pixel Array shape is rectangular - so this is valid
            buf.writeInt(width); // Width
            buf.writeInt(height); // Height
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    SkinPixel p = pixels[x][y];
                    buf.writeBytes(new byte[]{p.getR(), p.getG(), p.getB(), p.getA()}); // Writing pixel
                }
            }
        }
    }
}
