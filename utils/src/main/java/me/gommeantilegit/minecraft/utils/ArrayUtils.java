package me.gommeantilegit.minecraft.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ArrayUtils {

    @NotNull
    public static <T> T[] concat(@NotNull T[] first, @NotNull T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @NotNull
    public static double[][] unFlattenXY(@NotNull double[] flatArray, int sizeX, int sizeY) {
        double[][] unFlattened = new double[sizeX][sizeY];
        int i = 0;
        for (int xo = 0; xo < sizeX; xo++) {
            for (int zo = 0; zo < sizeY; zo++) {
                unFlattened[xo][zo] = flatArray[i++];
            }
        }
        return unFlattened;
    }

    @NotNull
    public static double[][][] unFlattenXZY(@NotNull double[] flatArray, int sizeX, int sizeY, int sizeZ) {
        double[][][] unFlattened = new double[sizeX][sizeY][sizeZ];
        int i = 0;
        for (int xo = 0; xo < sizeX; xo++) {
            for (int zo = 0; zo < sizeZ; zo++) {
                for (int yo = 0; yo < sizeY; yo++) {
                    unFlattened[xo][yo][zo] = flatArray[i++];
                }
            }
        }
        return unFlattened;
    }

    @NotNull
    public static double[] flattenXY(@NotNull double[][] array) {
        int sizeX = array.length, sizeY = array[0].length;
        double[] flatArray = new double[sizeX * sizeY];
        int i = 0;
        for (int xo = 0; xo < sizeX; xo++) {
            for (int zo = 0; zo < sizeY; zo++) {
                flatArray[i++] = array[xo][zo];
            }
        }
        return flatArray;
    }

    public static int max(int[] array) {
        int max = 0;
        for (int i : array) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }
}
