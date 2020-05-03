package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.generators.impl.surface;

import me.gommeantilegit.minecraft.world.generation.noise.minecraft.NoiseGeneratorOctaves2;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class HumidityGenerator {

    @NotNull
    private final NoiseGeneratorOctaves2 noiseGenerator;

    @NotNull
    private final NoiseGeneratorOctaves2 noiseGenerator2;

    public HumidityGenerator(long worldSeed) {
        this.noiseGenerator = new NoiseGeneratorOctaves2(new Random(worldSeed * 39811L), 4);
        this.noiseGenerator2 = new NoiseGeneratorOctaves2(new Random(worldSeed * 0x84a59L), 2);
    }

    @NotNull
    public double[] getHumidityValues(@NotNull double[] temperature, int x, int z, int width, int depth) {
        double[] humidity = this.noiseGenerator.generateNoiseOctaves(null, x, z, width, depth, 0.05000000074505806D, 0.05000000074505806D, 0.33333333333333331D);
        double[] noise = this.noiseGenerator2.generateNoiseOctaves(null, x, z, width, width, 0.25D, 0.25D, 0.58823529411764708D);
        int i1 = 0;
        for (int j1 = 0; j1 < width; j1++) {
            for (int k1 = 0; k1 < depth; k1++) {
                double d = noise[i1] * 1.1000000000000001D + 0.5D;
                double d1 = 0.01D;
                double d2 = 1.0D - d1;
                double d3 = (temperature[i1] * 0.14999999999999999D + 0.69999999999999996D) * d2 + d * d1;
                d1 = 0.002D;
                d2 = 1.0D - d1;
                double d4 = (humidity[i1] * 0.14999999999999999D + 0.5D) * d2 + d * d1;
                d3 = 1.0D - (1.0D - d3) * (1.0D - d3);
                if (d3 < 0.0D) {
                    d3 = 0.0D;
                }
                if (d4 < 0.0D) {
                    d4 = 0.0D;
                }
                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }
                if (d4 > 1.0D) {
                    d4 = 1.0D;
                }
                temperature[i1] = d3;
                humidity[i1] = d4;
                i1++;
            }

        }
        return humidity;
    }

}
