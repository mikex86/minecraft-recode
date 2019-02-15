package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.villages.generator.VillageGenerator;
import me.gommeantilegit.minecraft.world.generation.noise.PerlinNoise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldChunkGenerator extends ChunkGenerator {

    /**
     * Village generator instance. Null if villages are disable in world creator options.
     */
    @Nullable
    private final VillageGenerator villageGenerator;

    /**
     * Perlin noise instance for world creator
     */
    @NotNull
    private final PerlinNoise perlinNoise;

    /**
     * @param worldGenerator parent world generator to provide random instance and other attributes
     */
    public WorldChunkGenerator(@NotNull WorldGenerator worldGenerator) {
        super(worldGenerator);
        this.villageGenerator = worldGenerator.getWorldGenerationOptions().isVillages() ? new VillageGenerator(worldGenerator) : null;
        this.perlinNoise = new PerlinNoise(worldGenerator.getSeed());
    }

    @Override
    public void onChunkCreated(@NotNull Chunk chunk) {
        for (int xo = 0; xo < Chunk.CHUNK_SIZE; xo++) {
            for (int zo = 0; zo < Chunk.CHUNK_SIZE; zo++) {
                int x = chunk.getX() + xo;
                int z = chunk.getZ() + zo;
                int y = 10 + (int) (perlinNoise.getNoise(x / 50f, z / 50f) * 20);
                chunk.setBlockNoChangeWithoutWorldBlockChangerObject(x, y, z, Blocks.GRASS.getDefaultBlockState());
            }
        }
        if (this.villageGenerator != null) {
            this.villageGenerator.onChunkCreated(chunk);
        }
    }
}
