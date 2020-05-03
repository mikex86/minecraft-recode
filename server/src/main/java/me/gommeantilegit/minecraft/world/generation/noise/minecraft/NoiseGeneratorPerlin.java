package me.gommeantilegit.minecraft.world.generation.noise.minecraft;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

import java.util.Random;

public class NoiseGeneratorPerlin {

    public double xCoord;
    public double yCoord;
    public double zCoord;
    private int[] permutations;

    public NoiseGeneratorPerlin() {
        this(new Random());
    }

    public NoiseGeneratorPerlin(Random random) {
        permutations = new int[512];
        xCoord = random.nextDouble() * 256D;
        yCoord = random.nextDouble() * 256D;
        zCoord = random.nextDouble() * 256D;
        for (int i = 0; i < 256; i++) {
            permutations[i] = i;
        }

        for (int j = 0; j < 256; j++) {
            int k = random.nextInt(256 - j) + j;
            int l = permutations[j];
            permutations[j] = permutations[k];
            permutations[k] = l;
            permutations[j + 256] = permutations[j];
        }

    }

    public double generateNoise(double x, double y, double z) {
        double X = x + xCoord;
        double Y = y + yCoord;
        double Z = z + zCoord;
        int xi = (int) X;
        int yi = (int) Y;
        int zi = (int) Z;
        if (X < (double) xi) {
            xi--;
        }
        if (Y < (double) yi) {
            yi--;
        }
        if (Z < (double) zi) {
            zi--;
        }
        int Xi = xi & 0xff;
        int Yi = yi & 0xff;
        int Zi = zi & 0xff;
        X -= xi;
        Y -= yi;
        Z -= zi;
        double d6 = X * X * X * (X * (X * 6D - 15D) + 10D);
        double d7 = Y * Y * Y * (Y * (Y * 6D - 15D) + 10D);
        double d8 = Z * Z * Z * (Z * (Z * 6D - 15D) + 10D);
        int A = permutations[Xi] + Yi;
        int AA = permutations[A] + Zi;
        int AB = permutations[A + 1] + Zi;
        int B = permutations[Xi + 1] + Yi;
        int BA = permutations[B] + Zi;
        int BB = permutations[B + 1] + Zi;
        return lerp(d8, lerp(d7, lerp(d6, grad(permutations[AA], X, Y, Z), grad(permutations[BA], X - 1.0D, Y, Z)), lerp(d6, grad(permutations[AB], X, Y - 1.0D, Z), grad(permutations[BB], X - 1.0D, Y - 1.0D, Z))), lerp(d7, lerp(d6, grad(permutations[AA + 1], X, Y, Z - 1.0D), grad(permutations[BA + 1], X - 1.0D, Y, Z - 1.0D)), lerp(d6, grad(permutations[AB + 1], X, Y - 1.0D, Z - 1.0D), grad(permutations[BB + 1], X - 1.0D, Y - 1.0D, Z - 1.0D))));
    }

    public final double lerp(double a, double t, double b) {
        return t + a * (b - t);
    }

    public final double func_4110_a(int i, double d, double d1) {
        int j = i & 0xf;
        double d2 = (double) (1 - ((j & 8) >> 3)) * d;
        double d3 = j >= 4 ? j != 12 && j != 14 ? d1 : d : 0.0D;
        return ((j & 1) != 0 ? -d2 : d2) + ((j & 2) != 0 ? -d3 : d3);
    }

    public final double grad(int hash, double x, double y, double z) {
        int h = hash & 0xf;
        double u = h >= 8 ? y : x;
        double v = h >= 4 ? h != 12 && h != 14 ? z : x : y;
        return ((h & 1) != 0 ? -u : u) + ((h & 2) != 0 ? -v : v);
    }

    public double generateNoise(double x, double y) {
        return generateNoise(x, y, 0.0D);
    }

    public void generateNoise(double[] array, double x, double y, double z,
                              int width, int height, int depth, double xScale, double yScale,
                              double zScale, double sclFactor) {
        if (height == 1) {
            int arrayIndex = 0;
            double invSclFactor = 1.0D / sclFactor;
            for (int xOffset = 0; xOffset < width; xOffset++) {
                double xPos = (x + xOffset) * xScale + xCoord;
                int xPosi = (int) xPos;
                if (xPos < (double) xPosi) {
                    xPosi--;
                }
                int xPosiLimited = xPosi & 0xff;
                xPos -= xPosi;
                double xModified = xPos * xPos * xPos * (xPos * (xPos * 6D - 15D) + 10D);
                for (int zOffset = 0; zOffset < depth; zOffset++) {
                    double zPos = (z + zOffset) * zScale + zCoord;
                    int zPosi = (int) zPos;
                    if (zPos < (double) zPosi) {
                        zPosi--;
                    }
                    int zPosiLimited = zPosi & 0xff;
                    zPos -= zPosi;
                    double zModified = zPos * zPos * zPos * (zPos * (zPos * 6D - 15D) + 10D);
                    int perm0 = permutations[xPosiLimited] + 0;
                    int perm1 = permutations[perm0] + zPosiLimited;
                    int perm2 = permutations[xPosiLimited + 1] + 0;
                    int perm3 = permutations[perm2] + zPosiLimited;
                    double lerp0 = lerp(xModified, func_4110_a(permutations[perm1], xPos, zPos), grad(permutations[perm3], xPos - 1.0D, 0.0D, zPos));
                    double lerp1 = lerp(xModified, grad(permutations[perm1 + 1], xPos, 0.0D, zPos - 1.0D), grad(permutations[perm3 + 1], xPos - 1.0D, 0.0D, zPos - 1.0D));
                    double lerp2 = lerp(zModified, lerp0, lerp1);
                    array[arrayIndex++] += lerp2 * invSclFactor;
                }

            }

            return;
        }
        int bufferIndex = 0;
        double invScaleFactor = 1.0D / sclFactor;
        int lastYPosLimited = -1;
        double lerp0 = 0.0D;
        double lerp1 = 0.0D;
        double lerp2 = 0.0D;
        double lerp3 = 0.0D;
        for (int xOffset = 0; xOffset < width; xOffset++) {
            double xPos = (x + xOffset) * xScale + xCoord;
            int xPosi = (int) xPos;
            if (xPos < (double) xPosi) {
                xPosi--;
            }
            int xPosiLimited = xPosi & 0xff;
            xPos -= xPosi;
            double xModified = xPos * xPos * xPos * (xPos * (xPos * 6D - 15D) + 10D);
            for (int zOffset = 0; zOffset < depth; zOffset++) {
                double zPos = (z + zOffset) * zScale + zCoord;
                int zPosi = (int) zPos;
                if (zPos < (double) zPosi) {
                    zPosi--;
                }
                int zPosiLimited = zPosi & 0xff;
                zPos -= zPosi;
                double zModified = zPos * zPos * zPos * (zPos * (zPos * 6D - 15D) + 10D);
                for (int yOffset = 0; yOffset < height; yOffset++) {
                    double yPos = (y + yOffset) * yScale + yCoord;
                    int yPosi = (int) yPos;
                    if (yPos < (double) yPosi) {
                        yPosi--;
                    }
                    int yPosiLimited = yPosi & 0xff;
                    yPos -= yPosi;
                    double yModified = yPos * yPos * yPos * (yPos * (yPos * 6D - 15D) + 10D);
                    if (yOffset == 0 || yPosiLimited != lastYPosLimited) {
                        lastYPosLimited = yPosiLimited; // Seems like some last loop value to avoid some re-computation of some values
                        int perm0 = permutations[xPosiLimited] + yPosiLimited;
                        int perm1 = permutations[perm0] + zPosiLimited;
                        int perm2 = permutations[perm0 + 1] + zPosiLimited;
                        int perm3 = permutations[xPosiLimited + 1] + yPosiLimited;
                        int perm4 = permutations[perm3] + zPosiLimited;
                        int perm5 = permutations[perm3 + 1] + zPosiLimited;
                        lerp0 = lerp(xModified, grad(permutations[perm1], xPos, yPos, zPos), grad(permutations[perm4], xPos - 1.0D, yPos, zPos));
                        lerp1 = lerp(xModified, grad(permutations[perm2], xPos, yPos - 1.0D, zPos), grad(permutations[perm5], xPos - 1.0D, yPos - 1.0D, zPos));
                        lerp2 = lerp(xModified, grad(permutations[perm1 + 1], xPos, yPos, zPos - 1.0D), grad(permutations[perm4 + 1], xPos - 1.0D, yPos, zPos - 1.0D));
                        lerp3 = lerp(xModified, grad(permutations[perm2 + 1], xPos, yPos - 1.0D, zPos - 1.0D), grad(permutations[perm5 + 1], xPos - 1.0D, yPos - 1.0D, zPos - 1.0D));
                    }
                    double lerp01 = lerp(yModified, lerp0, lerp1);
                    double lerp02 = lerp(yModified, lerp2, lerp3);
                    double lerp03 = lerp(zModified, lerp01, lerp02);
                    array[bufferIndex++] += lerp03 * invScaleFactor;
                }

            }

        }

    }
}
