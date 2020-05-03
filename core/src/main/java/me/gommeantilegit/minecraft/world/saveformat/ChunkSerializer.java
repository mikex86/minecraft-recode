package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.utils.serialization.Serializer;
import me.gommeantilegit.minecraft.utils.serialization.buffer.BitByteBuffer;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;

public class ChunkSerializer implements Serializer<ChunkBase> {

    /**
     * Blocks instance
     */
    @NotNull
    private final Blocks blocks;

    public ChunkSerializer(@NotNull Blocks blocks) {
        this.blocks = blocks;
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
                buf.writeBytes(chunkSection.getPaletteData());
            }
        }
    }
}
