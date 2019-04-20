package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.block.state.BlockStateBase;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.utils.serialization.Serializer;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.utils.serialization.exception.DeserializationException;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkSection;
import me.gommeantilegit.minecraft.world.saveformat.data.ChunkData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;
import static me.gommeantilegit.minecraft.world.chunk.ChunkSection.CHUNK_SECTION_SIZE;

public class ChunkSerializer implements Serializer<ChunkBase> {

    /**
     * Minecraft instance
     */
    @NotNull
    private final AbstractMinecraft mc;

    public ChunkSerializer(@NotNull AbstractMinecraft mc) {
        this.mc = mc;
    }

    /**
     * Serializes the given chunk by writing a representation to the byte-buffer
     *
     * @param chunk the specified chunk
     * @param buf   the object buffer that the data should be written to
     */
    @Override
    public void serialize(@NotNull ChunkBase chunk, @NotNull BitByteBuffer buf) {
        for (ChunkSection chunkSection : chunk.getChunkSections()) {
            if (!chunkSection.isEmpty()) {
                for (int xo = 0; xo < CHUNK_SECTION_SIZE; xo++) {
                    for (int yo = 0; yo < CHUNK_SECTION_SIZE; yo++) {
                        for (int zo = 0; zo < CHUNK_SECTION_SIZE; zo++) {
                            int x = xo + chunk.getX(), y = yo + chunkSection.getStartHeight(), z = zo + chunk.getZ();
                            BlockStateBase blockState = chunk.getBlockState(x, y, z);
                            int blockID;
                            if (blockState == null) {
                                blockID = 0;
                            } else {
                                blockID = blockState.getBlock().getId();
                            }
                            buf.writeUnsignedShort(blockID);
                            if (blockState != null && blockState.getBlock().hasEnumFacing())
                                buf.writeByte((byte) blockState.getFacing().ordinal());
                        }
                    }
                }
            }
        }
    }

    /**
     * De-serializes the data of the byte buffer into a chunk data object
     *
     * @param buffer            the buffer to read data from
     * @param worldHeight       the height of the world and thus the chunk
     * @param chunkSectionsSent the array of states whether a given chunk section of the chunk has been sent in the packet
     * @return the de-serialized chunk data
     * @throws DeserializationException if de-serialization fails
     */
    @NotNull
    public ChunkData deserialize(@NotNull BitByteBuffer buffer, int worldHeight, boolean[] chunkSectionsSent) throws DeserializationException {
        buffer.useBytes();
        assert worldHeight % CHUNK_SECTION_SIZE == 0;
        assert chunkSectionsSent.length == worldHeight / CHUNK_SECTION_SIZE;
        BlockStateBase[][][] blockStates = new BlockStateBase[CHUNK_SIZE][worldHeight][CHUNK_SIZE];
        ChunkData chunkData = new ChunkData(worldHeight, blockStates);
        for (int i = 0; i < chunkSectionsSent.length; i++) {
            if (chunkSectionsSent[i]) {
                for (int xo = 0; xo < CHUNK_SECTION_SIZE; xo++) {
                    for (int yo = 0; yo < CHUNK_SECTION_SIZE; yo++) {
                        for (int zo = 0; zo < CHUNK_SECTION_SIZE; zo++) {
                            int y = (CHUNK_SECTION_SIZE * i) + yo;
                            int blockID = buffer.readUnsignedShort();
                            BlockBase block = mc.blocks.getBlockByID(blockID);
                            EnumFacing facing = EnumFacing.defaultFacing();
                            if (block != null && block.hasEnumFacing()) {
                                facing = EnumFacing.values()[buffer.readByte()];
                            }
                            BlockStateBase blockState;
                            if (block != null)
                                blockState = new BlockStateBase(block, facing);
                            else blockState = null;
                            blockStates[xo][y][zo] = blockState;
                        }
                    }
                }
            }
        }
        return chunkData;
    }
}
