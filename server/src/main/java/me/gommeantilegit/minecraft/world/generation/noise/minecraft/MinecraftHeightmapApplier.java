package me.gommeantilegit.minecraft.world.generation.noise.minecraft;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class MinecraftHeightmapApplier {

    @NotNull
    public static Block[] heightmapToBlocksFlat(@NotNull Blocks blocks, int const1, int width, int height, int depth, int heightmapWidth, int heightmapHeight, int heightmapDepth, @NotNull double[] heightmap, @NotNull double[] temperatures) {
        int seaLevel = 64;
        Block[] blocksOut = new Block[width * height * depth];
        for (int i1 = 0; i1 < const1; i1++) {
            for (int j1 = 0; j1 < const1; j1++) {
                for (int k1 = 0; k1 < 16; k1++) {
                    double d = 0.125D;
                    double d1 = heightmap[((i1 + 0) * heightmapDepth + (j1 + 0)) * heightmapHeight + (k1 + 0)];
                    double d2 = heightmap[((i1 + 0) * heightmapDepth + (j1 + 1)) * heightmapHeight + (k1 + 0)];
                    double d3 = heightmap[((i1 + 1) * heightmapDepth + (j1 + 0)) * heightmapHeight + (k1 + 0)];
                    double d4 = heightmap[((i1 + 1) * heightmapDepth + (j1 + 1)) * heightmapHeight + (k1 + 0)];
                    double d5 = (heightmap[((i1 + 0) * heightmapDepth + (j1 + 0)) * heightmapHeight + (k1 + 1)] - d1) * d;
                    double d6 = (heightmap[((i1 + 0) * heightmapDepth + (j1 + 1)) * heightmapHeight + (k1 + 1)] - d2) * d;
                    double d7 = (heightmap[((i1 + 1) * heightmapDepth + (j1 + 0)) * heightmapHeight + (k1 + 1)] - d3) * d;
                    double d8 = (heightmap[((i1 + 1) * heightmapDepth + (j1 + 1)) * heightmapHeight + (k1 + 1)] - d4) * d;
                    for (int l1 = 0; l1 < 8; l1++) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;
                        for (int i2 = 0; i2 < 4; i2++) {
                            int blockPosIndex = i2 + i1 * 4 << 11 | j1 * 4 << 7 | k1 * 8 + l1;
                            int c = '\200';
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;
                            for (int k2 = 0; k2 < 4; k2++) {
                                double d17 = temperatures[(i1 * 4 + i2) * 16 + (j1 * 4 + k2)];
                                Block block = null;
                                if (k1 * 8 + l1 < seaLevel) {
                                    if (d17 < 0.5D && k1 * 8 + l1 >= seaLevel - 1) {
                                        block = null; // TODO: CHANGE TO ICE WHEN ICE IS IMPLEMENTED
                                    } else {
                                        block = null; // TODO: CHANGE TO WATER WHEN WATER IS IMPLEMENTED
                                    }
                                }
                                if (d15 > 0.0D) {
                                    block = blocks.stone;
                                }
                                blocksOut[blockPosIndex] = block;
                                blockPosIndex += c;
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }

                }
            }
        }
        return blocksOut;
    }
}
