package me.gommeantilegit.minecraft.world.generation.generator.impl.overworld.generators.impl.surface;

import me.gommeantilegit.minecraft.world.generation.noise.minecraft.NoiseGeneratorOctaves2;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TemperatureGenerator {

    @NotNull
    private final NoiseGeneratorOctaves2 noiseGenerator;

    public TemperatureGenerator(long worldSeed) {
        Random random = new Random(worldSeed * 9871L);
        this.noiseGenerator = new NoiseGeneratorOctaves2(random, 4);
    }

    @NotNull
    public double[] getTemperatures(int x, int z, int width, int depth) {
        return this.noiseGenerator.generateNoiseOctaves(null, x, z, width, depth, 0.02500000037252903D, 0.02500000037252903D, 0.25D);
    }
}
