package me.gommeantilegit.minecraft.world.generation.noise.minecraft;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorOctaves {

    private NoiseGeneratorPerlin[] generatorCollection;
    private int numGenerators;

    public NoiseGeneratorOctaves(Random random, int numGenerators) {
        this.numGenerators = numGenerators;
        generatorCollection = new NoiseGeneratorPerlin[numGenerators];
        for (int j = 0; j < numGenerators; j++) {
            generatorCollection[j] = new NoiseGeneratorPerlin(random);
        }

    }

    public double generateNoise(double x, double y) {
        double noiseValue = 0.0D;
        double sclFactor = 1.0D;
        for (int i = 0; i < numGenerators; i++) {
            noiseValue += generatorCollection[i].generateNoise(x * sclFactor, y * sclFactor) / sclFactor;
            sclFactor /= 2D;
        }

        return noiseValue;
    }

    public double[] generateNoiseOctaves(double[] array, double x, double y, double z,
                                         int width, int height, int depth, double xScale, double yScale,
                                         double zScale) {
        if (array == null) {
            array = new double[width * height * depth];
        } else {
            Arrays.fill(array, 0.0D);
        }
        double sclFactor = 1.0D;
        for (int i = 0; i < numGenerators; i++) {
            generatorCollection[i].generateNoise(array, x, y, z, width, height, depth, xScale * sclFactor, yScale * sclFactor, zScale * sclFactor, sclFactor);
            sclFactor /= 2D;
        }

        return array;
    }

    public double[] generateNoiseOctaves(double[] array, int x, int y, int width, int depth, double xScale,
                                         double zScale) {
        return generateNoiseOctaves(array, x, 10D, y, width, 1, depth, xScale, 1.0D, zScale);
    }
}
