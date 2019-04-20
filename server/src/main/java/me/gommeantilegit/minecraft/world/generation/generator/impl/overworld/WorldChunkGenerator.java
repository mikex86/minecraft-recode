package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.villages.generator.VillageGenerator;
import me.gommeantilegit.minecraft.world.generation.noise.PerlinNoise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SideOnly(side = Side.SERVER)
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

    @NotNull
    private final ServerMinecraft mc;

    /**
     * @param worldGenerator parent world generator to provide random instance and other attributes
     * @param mc        minecraft instance
     */
    public WorldChunkGenerator(@NotNull WorldGenerator worldGenerator, @NotNull ServerMinecraft mc) {
        super(worldGenerator);
        this.villageGenerator = worldGenerator.getWorldGenerationOptions().isVillages() ? new VillageGenerator(worldGenerator) : null;
        this.perlinNoise = new PerlinNoise(worldGenerator.getSeed());
        this.mc = mc;
    }

    @Override
    public void onChunkCreated(@NotNull ServerChunk chunk) {
        for (int xo = 0; xo < ChunkBase.CHUNK_SIZE; xo++) {
            for (int zo = 0; zo < ChunkBase.CHUNK_SIZE; zo++) {
                int x = chunk.getX() + xo;
                int z = chunk.getZ() + zo;
                int y = 50 + (int) (perlinNoise.getNoise(x / 50f, z / 50f) * 20);
                chunk.setBlockWithoutWorldBlockChangerObject(x, y, z, mc.blocks.grass.getDefaultBlockState());
                for (int yo = 1; yo < y; yo++) {
                    chunk.setBlockWithoutWorldBlockChangerObject(x, yo, z, mc.blocks.stone.getDefaultBlockState());
                }
            }
        }
        if (this.villageGenerator != null) {
            this.villageGenerator.onChunkCreated(chunk);
        }
    }
}
