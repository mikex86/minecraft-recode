package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.GeneratorBase;
import me.gommeantilegit.minecraft.world.generation.noise.PerlinNoise;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SideOnly(side = Side.SERVER)
public class WorldChunkGenerator extends ChunkGenerator {
    /**
     * Perlin noise instance for world creator
     */
    @NotNull
    private final PerlinNoise perlinNoise;

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ServerMinecraft mc;

    /**
     * Chunk generators
     */
    @NotNull
    private final GeneratorBase[] generators = new GeneratorBase[]{

    };

    /**
     * @param worldGenerator parent world generator to provide random instance and other attributes
     * @param mc             minecraft instance
     */
    public WorldChunkGenerator(@NotNull WorldGenerator worldGenerator, @NotNull ServerMinecraft mc) {
        super(worldGenerator);
        this.perlinNoise = new PerlinNoise(worldGenerator.getSeed());
        this.mc = mc;
    }

    @Override
    public void onChunkCreated(@NotNull ChunkBase chunk) {
        generateTerrain(chunk.getX(), chunk.getZ(), worldGenerator.getRandom());
    }

    private void generateTerrain(int chunkX, int chunkZ, @NotNull Random random) {

    }
}
