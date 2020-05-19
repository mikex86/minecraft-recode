package me.gommeantilegit.minecraft.world.generation.generator;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.WorldChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.impl.superflat.SuperFlatChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SideOnly(side = Side.SERVER)
public class WorldGenerator implements ServerWorld.OnServerChunkCreationListener {

    /**
     * Random instance for creator
     */
    @NotNull
    private final Random random;

    /**
     * ServerWorld type enum value
     */
    @NotNull
    private final WorldGenerationOptions.WorldType worldType;

    /**
     * ServerWorld Generation seed
     */
    private final long seed;

    @NotNull
    private final ChunkGenerator chunkGenerator;

    /**
     * Options regarding world creator
     */
    @NotNull
    private final WorldGenerationOptions worldGenerationOptions;

    public WorldGenerator(@NotNull ServerMinecraft mc, @NotNull WorldGenerationOptions worldGenerationOptions) {
        this.seed = worldGenerationOptions.getSeed();
        this.random = new Random(seed);
        this.worldType = worldGenerationOptions.getWorldType();
        this.worldGenerationOptions = worldGenerationOptions;
        switch (worldType) {
            case SUPER_FLAT:
                chunkGenerator = new SuperFlatChunkGenerator(this, mc);
                break;
            case OVERWORLD:
                chunkGenerator = new WorldChunkGenerator(this, mc);
                break;
            default:
                throw new IllegalStateException("Invalid world type: " + worldType);
        }
    }

    @Override
    public void onChunkCreated(@NotNull ChunkBase chunk) {
        this.chunkGenerator.onChunkCreated(chunk);
    }

    @NotNull
    public WorldGenerationOptions.WorldType getWorldType() {
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

}
