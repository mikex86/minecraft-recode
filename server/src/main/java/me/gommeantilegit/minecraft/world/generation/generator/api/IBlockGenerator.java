package me.gommeantilegit.minecraft.world.generation.generator.api;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * Represents a generator generating blocks on chunk creation
 */
public interface IBlockGenerator {

    /**
     * Called on chunk generation
     *
     * @param random the random instance
     * @param chunk  the chunk to generate into
     * @param blocks blocks instance
     */
    void generateBlocks(@NotNull Random random, @NotNull ChunkBase chunk, @NotNull Blocks blocks);

}
