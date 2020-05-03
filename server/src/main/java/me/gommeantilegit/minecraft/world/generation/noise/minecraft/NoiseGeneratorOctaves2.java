package me.gommeantilegit.minecraft.world.generation.noise.minecraft;

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorOctaves2 {

    private NoiseGenerator2[] generators;
    private int numGenerators;

    public NoiseGeneratorOctaves2(Random random, int numGenerators) {
        this.numGenerators = numGenerators;
        generators = new NoiseGenerator2[numGenerators];
        for (int i = 0; i < numGenerators; i++) {
            generators[i] = new NoiseGenerator2(random);
        }

    }

    public double[] generateNoiseOctaves(double[] array, double x, double z, int width, int depth,
                                         double d2, double d3, double d4) {
        return generateNoiseOctaves(array, x, z, width, depth, d2, d3, d4, 0.5D);
    }

    public double[] generateNoiseOctaves(double[] array, double x, double y, int width, int depth,
                                         double d2, double d3, double d4, double d5) {
        d2 /= 1.5D;
        d3 /= 1.5D;
        if (array == null || array.length < width * depth) {
            array = new double[width * depth];
        } else {
            Arrays.fill(array, 0.0D);
        }
        double sclFactor1 = 1.0D;
        double sclFactor2 = 1.0D;
        for (int l = 0; l < numGenerators; l++) {
            generators[l].generateNoise(array, x, y, width, depth, d2 * sclFactor2, d3 * sclFactor2, 0.55000000000000004D / sclFactor1);
            sclFactor2 *= d4;
            sclFactor1 *= d5;
        }

        return array;
    }
}
