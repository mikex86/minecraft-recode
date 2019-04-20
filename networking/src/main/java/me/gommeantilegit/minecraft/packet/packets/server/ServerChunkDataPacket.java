package me.gommeantilegit.minecraft.packet.packets.server;

import com.badlogic.gdx.math.Vector2;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.gommeantilegit.minecraft.utils.MathHelper.humanReadableByteCount;
import static me.gommeantilegit.minecraft.utils.io.IOUtils.compress;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

/**
 * Packet storing data representing a chunk
 * Created on server - sent by the server.
 */
@PacketInfo(side = PacketInfo.PacketSide.SERVER, encoder = ServerChunkDataPacket.Encoder.class, decoder = ServerChunkDataPacket.Decoder.class)
public class ServerChunkDataPacket extends ServerPacket {

    /**
     * PacketID
     */
    @PacketID
    public static final short PACKET_ID = SERVER_PACKET_BIT | 4;

    /**
     * Height of the world of the chunk
     */
    private final int worldHeight;

    /**
     * An array of states, if a given chunk region (16x16x16) has been sent in this packet. Empty chunk sections are not sent because the chunk
     * is full air.
     */
    private final boolean[] chunkSectionsSent;

    /**
     * Chunk Origin
     */
    @NotNull
    private final Vector2 chunkOrigin;

    /**
     * Byte data representing the chunk.
     * This is always a compressed array and must be decompressed when it is read
     */
    @NotNull
    private final byte[] chunkData;

    /**
     * @param serverChannel the server channel that sent this packet
     * @param chunkOrigin   chunk origin position of the chunk
     * @param chunkBase     the chunk which data should be sent
     */
    public ServerChunkDataPacket(@Nullable Channel serverChannel, @NotNull Vector2 chunkOrigin, @NotNull ChunkBase chunkBase) {
        super(PACKET_ID, serverChannel);
        this.worldHeight = chunkBase.getHeight();
        this.chunkOrigin = chunkOrigin;
        assert chunkBase.getChunkSections().length == chunkBase.getHeight() * CHUNK_SECTION_SIZE;
        this.chunkSectionsSent = new boolean[chunkBase.getChunkSections().length];
        for (int i = 0; i < chunkBase.getChunkSections().length; i++) {
            chunkSectionsSent[i] = !chunkBase.getChunkSections()[i].isEmpty();
        }
        BitByteBuffer buf = new BitByteBuffer();
        chunkBase.mc.chunkSerializer.serialize(chunkBase, buf);
        this.chunkData = compress(buf.retrieveBytes());
    }

    /**
     * @param serverChannel     the server channel that sent this packet
     * @param origin            chunk origin position of the chunk
     * @param worldHeight       the height of the world and thus of the chunk
     * @param chunkSectionsSent the states if a given chunk region is sent
     * @param chunkData         the chunk data that represents the chunk data for the specified chunk
     */
    public ServerChunkDataPacket(@Nullable Channel serverChannel, @NotNull Vector2 origin, int worldHeight, boolean[] chunkSectionsSent, @NotNull byte[] chunkData) {
        super(PACKET_ID, serverChannel);
        this.chunkOrigin = origin;
        this.worldHeight = worldHeight;
        this.chunkSectionsSent = chunkSectionsSent;
        this.chunkData = chunkData;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    @NotNull
    public Vector2 getChunkOrigin() {
        return chunkOrigin;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public boolean[] getChunkSectionsSent() {
        return chunkSectionsSent;
    }

    @SideOnly(side = Side.SERVER)
    public static class Encoder extends PacketEncoder<ServerChunkDataPacket> {

        @Override
        public void serialize(@NotNull ServerChunkDataPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeInt(packet.worldHeight); // Writing world height
            buf.writeVector2(packet.chunkOrigin); // Writing chunk origin position
            buf.writeInt(packet.chunkSectionsSent.length);
            buf.useBits();
            for (int i = 0; i < packet.chunkSectionsSent.length; i++) {
                buf.writeBit(packet.chunkSectionsSent[i] ? 1 : 0);
            }
            buf.useBytes();
            buf.writeBytes(packet.getChunkData());
        }

    }

    @SideOnly(side = Side.CLIENT)
    public static class Decoder extends PacketDecoder<ServerChunkDataPacket> {
        /**
         * Parses a received byte buffer and parses it into a {@link ServerChunkDataPacket} instance
         *
         * @param buffer  the byte buffer to be read and parsed into a {@link ServerChunkDataPacket} instance
         * @param channel the connection channel
         */
        @Nullable
        @Override
        public ServerChunkDataPacket deserialize(@NotNull BitByteBuffer buffer, Channel channel) throws PacketDecodingException {
            int worldHeight = buffer.readInt();
            Vector2 origin = buffer.readVector2();
            int chunkSectionsLength = buffer.readInt();
            assert chunkSectionsLength == worldHeight / CHUNK_SECTION_SIZE;
            boolean[] chunkSectionsSent = new boolean[chunkSectionsLength];
            buffer.useBits();
            for (int i = 0; i < chunkSectionsSent.length; i++) {
                chunkSectionsSent[i] = buffer.readBit() == 1;
            }
            buffer.useBytes();
            int readIndex = (buffer.getReadingBitIndex() / 8);
            int maxIndex = buffer.bytes();
            byte[] chunkData = buffer.readBytes(maxIndex - readIndex);
            return new ServerChunkDataPacket(channel, origin, worldHeight, chunkSectionsSent, chunkData);
        }
    }
}
