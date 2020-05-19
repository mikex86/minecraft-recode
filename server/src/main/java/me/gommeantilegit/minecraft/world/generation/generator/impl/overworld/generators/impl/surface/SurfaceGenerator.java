package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.generators.impl.surface;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.change.BlockStateSemaphoreBase;
import me.gommeantilegit.minecraft.world.generation.generator.api.IBlockGenerator;
import me.gommeantilegit.minecraft.world.generation.noise.minecraft.BiomeNoiseApplier;
import me.gommeantilegit.minecraft.world.generation.noise.minecraft.MinecraftHeightmapApplier;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class SurfaceGenerator implements IBlockGenerator {

    @NotNull
    private final HeightMapGenerator heightMapGenerator;

    @NotNull
    private final TemperatureGenerator temperatureGenerator;

    @NotNull
    private final HumidityGenerator humidityGenerator;


//    @NotNull
//    private final ForkJoinPool chunkEmulationTaskPool = new ForkJoinPool();

    public SurfaceGenerator(long worldSeed) {
        this.heightMapGenerator = new HeightMapGenerator(worldSeed);
        this.temperatureGenerator = new TemperatureGenerator(worldSeed);
        this.humidityGenerator = new HumidityGenerator(worldSeed);
    }

    @Override
    public void generateBlocks(@NotNull Random random, @NotNull ChunkBase chunk, @NotNull Blocks blocks) {
        Vec2i origin = chunk.getChunkOrigin();

//        List<ForkJoinTask<?>> tasks = new ArrayList<>((ChunkBase.CHUNK_SIZE * ChunkBase.CHUNK_SIZE) / (16 * 16));
        for (int chunkX = origin.getX(); chunkX < chunk.getX() + ChunkBase.CHUNK_SIZE; chunkX += 16) {
            for (int chunkZ = origin.getY(); chunkZ < chunk.getZ() + ChunkBase.CHUNK_SIZE; chunkZ += 16) {
                final int finalChunkX = chunkX, finalChunkZ = chunkZ;
//                ForkJoinTask<?> task = chunkEmulationTaskPool.submit(() -> emulateMinecraftChunkSize(blocks, finalChunkX, finalChunkZ, chunk));
//                tasks.add(task);
                emulateMinecraftChunkSize(blocks, finalChunkX, finalChunkZ, chunk);
            }
        }
//        for (ForkJoinTask<?> task : tasks) {
//            try {
//                task.get();
//            } catch (InterruptedException | ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    private void emulateMinecraftChunkSize(@NotNull Blocks blocks, int startX, int startZ, ChunkBase chunk) {
        // Minecraft constants
        int const1 = 4;
        int width = const1 + 1, height = 17, depth = const1 + 1;

        double[] temperaturesFlat = this.temperatureGenerator.getTemperatures(startX, startZ, 16, 16);
        double[] humidityValuesFlat = this.humidityGenerator.getHumidityValues(temperaturesFlat, startX, startZ, 16, 16);

        double[] heightmapFlat = this.heightMapGenerator.generateHeightmap(const1, temperaturesFlat, humidityValuesFlat, startX, startZ, width, height, depth);
        List<double[]> biomeBasedNoise = this.heightMapGenerator.generateBiomeBasedNoise(startX, startZ);

        // 128 because of beta height limit and not breaking flat array indexing
        int topY = 128;
        Block[] blocksFlat = MinecraftHeightmapApplier.heightmapToBlocksFlat(blocks, const1, 16, topY, 16, width, height, depth, heightmapFlat, temperaturesFlat);
        Random chunkRandom = new Random((long) (startX / 16) * 0x4f9939f508L + (long) (startZ / 16) * 0x1ef1565bd5L);
        BiomeNoiseApplier.applyBiomeNoise(chunkRandom, blocks, biomeBasedNoise, blocksFlat);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < topY; y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = blocksFlat[(x) * (topY * 16) + (z) * topY + (y)];
                    if (block == null)
                        continue;
                    chunk.writeBlockChange(startX + x - chunk.getX(), y, startZ + z - chunk.getZ(), block.getDefaultBlockState());
                }
            }
        }
    }

}
