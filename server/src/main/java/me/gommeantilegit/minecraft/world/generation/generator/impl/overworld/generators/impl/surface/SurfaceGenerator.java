package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.generators.impl.surface;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.utils.ArrayUtils;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.api.IBlockGenerator;
import me.gommeantilegit.minecraft.world.generation.noise.minecraft.BiomeNoiseApplier;
import me.gommeantilegit.minecraft.world.generation.noise.minecraft.MinecraftHeightmapApplier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;

public class SurfaceGenerator implements IBlockGenerator {

    @NotNull
    private final HeightMapGenerator heightMapGenerator;

    @NotNull
    private final TemperatureGenerator temperatureGenerator;

    @NotNull
    private final HumidityGenerator humidityGenerator;

    public SurfaceGenerator(long worldSeed) {
        this.heightMapGenerator = new HeightMapGenerator(worldSeed);
        this.temperatureGenerator = new TemperatureGenerator(worldSeed);
        this.humidityGenerator = new HumidityGenerator(worldSeed);
    }

    @Override
    public void generateBlocks(@NotNull Random random, @NotNull ChunkBase chunk, @NotNull Blocks blocks) {
        Vec2i origin = chunk.getChunkOrigin();
        int chunkX = origin.getX(), chunkZ = origin.getY();

        // Minecraft constants
        int const1 = 4;
        int width = const1 + 1, height = 17, depth = const1 + 1;

        double[] temperaturesFlat = this.temperatureGenerator.getTemperatures(chunkX, chunkZ, CHUNK_SIZE, CHUNK_SIZE);
        double[] humidityValuesFlat = this.humidityGenerator.getHumidityValues(temperaturesFlat, chunkX, chunkZ, CHUNK_SIZE, CHUNK_SIZE);

        double[] heightmapFlat = this.heightMapGenerator.generateHeightmap(const1, temperaturesFlat, humidityValuesFlat, chunkX, chunkZ, width, height, depth);
        List<double[]> biomeBasedNoise = this.heightMapGenerator.generateBiomeBasedNoise(chunkX, chunkZ);

        // 128 because of beta height limit and not breaking flat array indexing
        int topY = 128;
        Block[] blocksFlat = MinecraftHeightmapApplier.heightmapToBlocksFlat(blocks, const1, CHUNK_SIZE, topY, CHUNK_SIZE, width, height, depth, heightmapFlat, temperaturesFlat);
        Random chunkRandom = new Random((long) (chunkX / CHUNK_SIZE) * 0x4f9939f508L + (long) (chunkZ / CHUNK_SIZE) * 0x1ef1565bd5L);
        BiomeNoiseApplier.applyBiomeNoise(chunkRandom, blocks, biomeBasedNoise, blocksFlat);
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < topY; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    Block block = blocksFlat[x * (topY * CHUNK_SIZE) + z * topY + y];
                    if (block == null)
                        continue;
                    chunk.setBlock(chunkX + x, y, chunkZ + z, block.getDefaultBlockState());
                }
            }
        }
    }

}
