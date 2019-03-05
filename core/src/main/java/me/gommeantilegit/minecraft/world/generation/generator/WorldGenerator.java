package me.gommeantilegit.minecraft.world.generation.generator;

import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.WorldChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.superflat.SuperFlatChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WorldGenerator implements World.OnChunkCreationListener {

    /**
     * Random instance for creator
     */
    @NotNull
    private final Random random;

    /**
     * World type enum value
     */
    @NotNull
    private final WorldType worldType;

    /**
     * World Generation seed
     */
    private final long seed;

    @NotNull
    private final ChunkGenerator chunkGenerator;

    /**
     * Options regarding world creator
     */
    @NotNull
    private final WorldGenerationOptions worldGenerationOptions;

    public WorldGenerator(long seed, @NotNull WorldType worldType, @NotNull WorldGenerationOptions worldGenerationOptions) {
        this.seed = seed;
        this.random = new Random(seed);
        this.worldType = worldType;
        this.worldGenerationOptions = worldGenerationOptions;
        switch (worldType) {
            case SUPER_FLAT:
                chunkGenerator = new SuperFlatChunkGenerator(this);
                break;
            case OVERWORLD:
                chunkGenerator = new WorldChunkGenerator(this);
                break;
            default:
                throw new IllegalStateException("Invalid world type: " + worldType);
        }
    }

    @Override
    public void onChunkCreated(@NotNull Chunk chunk) {
        this.chunkGenerator.onChunkCreated(chunk);
    }

    @NotNull
    public WorldType getWorldType() {
        return worldType;
    }

    @NotNull
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @NotNull
    public Random getRandom() {
        return random;
    }

    public long getSeed() {
        return seed;
    }

    @NotNull
    public WorldGenerationOptions getWorldGenerationOptions() {
        return worldGenerationOptions;
    }

    public enum WorldType {
        OVERWORLD, SUPER_FLAT
    }
}
