package me.gommeantilegit.minecraft.packet.packets.server;

import com.badlogic.gdx.math.Vector2;
import io.netty.channel.Channel;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.state.palette.GlobalBlockStatePalette;
import me.gommeantilegit.minecraft.packet.ServerPacket;
import me.gommeantilegit.minecraft.packet.annotations.PacketID;
import me.gommeantilegit.minecraft.packet.annotations.PacketInfo;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.packet.exception.PacketDecodingException;
import me.gommeantilegit.minecraft.packet.proc.PacketDecoder;
import me.gommeantilegit.minecraft.packet.proc.PacketEncoder;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkSection;
import me.gommeantilegit.minecraft.world.saveformat.ChunkFragmenter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.BitSet;
import java.util.List;

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
    @NotNull
    private final BitSet fragmentsSent;

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

    // TODO: RE-WORK WITH CACHE FOR SERIALIZED DATA (CHANGE ON MARK DIRTY)
    public ServerChunkDataPacket(@Nullable Channel serverChannel, @NotNull Vector2 chunkOrigin, @NotNull ChunkBase chunkBase) {
        super(PACKET_ID, serverChannel);
        this.worldHeight = chunkBase.getHeight();
        this.chunkOrigin = chunkOrigin;
        List<ChunkSection> sections = chunkBase.getChunkSections();
        assert sections.size() == chunkBase.getHeight() / CHUNK_SECTION_SIZE;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // FIXME: 5/10/2020 THIS IS UGLY
            this.fragmentsSent = chunkBase.getWorld().mc.getChunkFragmenter().fragmentChunk(chunkBase.getBlockStatePalette(), chunkBase, stream);
        } catch (IOException e) {
            throw new RuntimeException(e); // Will never happen on ByteArrayOutputStream
        }
        this.chunkData = compress(stream.toByteArray());
    }

    public ServerChunkDataPacket(@Nullable Channel serverChannel, @NotNull Vector2 origin, int worldHeight, @NotNull BitSet fragmentsSent, @NotNull byte[] chunkData) {
        super(PACKET_ID, serverChannel);
        this.chunkOrigin = origin;
        this.worldHeight = worldHeight;
        this.fragmentsSent = fragmentsSent;
        this.chunkData = chunkData;
    }

    @NotNull
    public BitSet getFragmentsSent() {
        return fragmentsSent;
    }

    @NotNull
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

    @SideOnly(side = Side.SERVER)
    public static class Encoder extends PacketEncoder<ServerChunkDataPacket> {

        @Override
        public void serialize(@NotNull ServerChunkDataPacket packet, @NotNull BitByteBuffer buf) {
            buf.writeInt(packet.worldHeight); // Writing world height
            buf.writeVector2(packet.chunkOrigin); // Writing chunk origin position
            byte[] bitSetBytes = packet.fragmentsSent.toByteArray();
            buf.writeInt(bitSetBytes.length);
            buf.writeBytes(bitSetBytes);
            byte[] chunkData = packet.getChunkData();
            buf.writeInt(chunkData.length);
            buf.writeBytes(chunkData);
//            System.out.println("Chunk Data packet: " + humanReadableByteCount(buf.getWritingBitIndex() / Byte.SIZE, true));
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
        public ServerChunkDataPacket deserialize(@NotNull BitByteBuffer buffer, @Nullable Channel channel) throws PacketDecodingException {
            int worldHeight = buffer.readInt();
            Vector2 origin = buffer.readVector2();
            int numBitSetBytes = buffer.readInt();
            byte[] bitSetBytes = buffer.readBytes(numBitSetBytes);
            BitSet fragmentsSent = BitSet.valueOf(bitSetBytes);
            int chunkDataLength = buffer.readInt();
            byte[] chunkData = buffer.readBytes(chunkDataLength);
            return new ServerChunkDataPacket(channel, origin, worldHeight, fragmentsSent, chunkData);
        }
    }
}
