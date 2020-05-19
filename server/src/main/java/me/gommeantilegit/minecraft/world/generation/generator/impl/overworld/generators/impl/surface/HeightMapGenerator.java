package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.generators.impl.surface;

import me.gommeantilegit.minecraft.world.generation.noise.minecraft.MinecraftHeightmapAssembler;
import me.gommeantilegit.minecraft.world.generation.noise.minecraft.NoiseGeneratorOctaves;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static me.gommeantilegit.minecraft.world.chunk.ChunkBase.CHUNK_SIZE;

public class HeightMapGenerator {

    @NotNull
    private final NoiseValuesGenerator noiseValuesGenerator;

    public HeightMapGenerator(long worldSeed) {
        this.noiseValuesGenerator = new NoiseValuesGenerator(worldSeed);
    }

    @NotNull
    public double[] generateHeightmap(int scaleFactor, @NotNull double[] temperaturesFlat, @NotNull double[] humidityValuesFlat, int x, int z, int width, int height, int depth) {
        x /= 16;
        z /= 16;

        // Minecraft constants (probably noise scale values on the x and z axis)
        double const1 = 684.41200000000003D;
        double const2 = 684.41200000000003D;

        List<double[]> noise = this.noiseValuesGenerator.generateNoise(x * scaleFactor, 0, z * scaleFactor, width, height, depth, const1, const2);

        double[] heightmapFlat = MinecraftHeightmapAssembler.assembleHeightmap(width, height, depth, temperaturesFlat, humidityValuesFlat,
                noise.get(0), noise.get(1), noise.get(2), noise.get(3), noise.get(4));
//        return ArrayUtils.unFlattenXZY(heightmapFlat, width, height, depth); // TODO: maybe change component ordering if something is off
        return heightmapFlat;
    }

    @NotNull
    public List<double[]> generateBiomeBasedNoise(int x, int z) {
        return this.noiseValuesGenerator.generateBiomeBasedNoise(x, z);
    }


    private static final class NoiseValuesGenerator {

        @NotNull
        private final NoiseGeneratorOctaves generator16, generator16v2, generator8, generator4, generator4v2, generator10, generator16v3;

        public NoiseValuesGenerator(long worldSeed) {
            Random random = new Random(worldSeed);
            this.generator16 = new NoiseGeneratorOctaves(random, 16);
            this.generator16v2 = new NoiseGeneratorOctaves(random, 16);
            this.generator8 = new NoiseGeneratorOctaves(random, 8);
            this.generator4 = new NoiseGeneratorOctaves(random, 4);
            this.generator4v2 = new NoiseGeneratorOctaves(random, 4);
            this.generator10 = new NoiseGeneratorOctaves(random, 10);
            this.generator16v3 = new NoiseGeneratorOctaves(random, 16);
        }

        /**
         * the list of flat noise values generated by the noise octave generators according to minecraft terrain generation logic
         */
        @NotNull
        public List<double[]> generateNoise(int x, int y, int z, int width, int height, int depth, double const1, double const2) {
            double[] noise1Flat = this.generator10.generateNoiseOctaves(null, x, z, width, depth, 1.121D, 1.121D);
            double[] noise2Flat = this.generator16v3.generateNoiseOctaves(null, x, z, width, depth, 200D, 200D);
            double[] noise3Flat = this.generator8.generateNoiseOctaves(null, x, y, z, width, height, depth, const1 / 80D, const2 / 160D, const1 / 80D);
            double[] noise4Flat = this.generator16.generateNoiseOctaves(null, x, y, z, width, height, depth, const1, const2, const1);
            double[] noise5Flat = this.generator16v2.generateNoiseOctaves(null, x, y, z, width, height, depth, const1, const2, const1);
            return Arrays.asList(noise1Flat, noise2Flat, noise3Flat, noise4Flat, noise5Flat);
        }

        @NotNull
        public List<double[]> generateBiomeBasedNoise(int x, int z) {
            double const1 = 0.03125D;
            double[] sandNoise = this.generator4.generateNoiseOctaves(null, x, z, 0.0D, 16, 16, 1, const1, const1, 1.0D);
            double[] gravelNoise = this.generator4.generateNoiseOctaves(null, x, 109.0134D, z, 16, 1, 16, const1, 1.0D, const1);
            double[] stoneNoise = this.generator4v2.generateNoiseOctaves(null, x, z, 0.0D, 16, 16, 1, const1 * 2D, const1 * 2D, const1 * 2D);
            return Arrays.asList(sandNoise, gravelNoise, stoneNoise);
        }
    }
}
