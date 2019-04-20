package me.gommeantilegit.minecraft.world.generation.generator.api;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Represents a generator generating blocks on chunk creation
 */
public abstract class GeneratorBase {

    /**
     * Called on chunk generation
     * @param random the random instance
     * @param chunkX the chunk x coordinate (origin)
     * @param chunkZ the chunk z coordinate (origin)
     */
    public abstract void generateBlocks(@NotNull Random random, int chunkX, int chunkZ);

}
