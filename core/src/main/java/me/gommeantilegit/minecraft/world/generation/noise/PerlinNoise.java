package me.gommeantilegit.minecraft.world.generation.noise;

import java.util.Random;

/**
 * A Perlin Noise implementation
 */
public class PerlinNoise implements Noise3D {

    private final int permutation[] = new int[512];

    /**
     * @param seed seed for creation of permutation values
     */
    public PerlinNoise(int seed) {
        Random random = new Random(seed);
        //Generating permutation using a random instance
        for (int i = 0; i < 256; i++) permutation[256 + i] = permutation[i] = random.nextInt(256);
    }

    /**
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the noise value for the given coordinate
     */
    @Override
    public double getNoise(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255,
                Y = (int) Math.floor(y) & 255,
                Z = (int) Math.floor(z) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = fade(x),
                v = fade(y),
                w = fade(z);
        int A = permutation[X] + Y, AA = permutation[A] + Z, AB = permutation[A + 1] + Z,
                B = permutation[X + 1] + Y, BA = permutation[B] + Z, BB = permutation[B + 1] + Z;

        return lerp(w, lerp(v, lerp(u, grad(permutation[AA], x, y, z),
                grad(permutation[BA], x - 1, y, z)),
                lerp(u, grad(permutation[AB], x, y - 1, z),
                        grad(permutation[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(permutation[AA + 1], x, y, z - 1),
                        grad(permutation[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(permutation[AB + 1], x, y - 1, z - 1),
                                grad(permutation[BB + 1], x - 1, y - 1, z - 1))));
    }

    /**
     * Interpolation function f(t) = 6t^5 - 15t^4 + 10t^3
     * @param t value to be interpolated
     * @return the interpolated value of t.
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y,
                v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
