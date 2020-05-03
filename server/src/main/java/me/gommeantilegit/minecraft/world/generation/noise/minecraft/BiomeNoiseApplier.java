package me.gommeantilegit.minecraft.world.generation.noise.minecraft;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class BiomeNoiseApplier {

    public static void applyBiomeNoise(@NotNull Random chunkRandom, @NotNull Blocks blocks, @NotNull List<double[]> biomeBasedNoise, @NotNull Block[] blocksFlat) {

        byte seaLevel = 64;
        double[] sandNoise = biomeBasedNoise.get(0), gravelNoise = biomeBasedNoise.get(1), stoneNoise = biomeBasedNoise.get(2);
        for (int k = 0; k < 16; k++) {
            for (int l = 0; l < 16; l++) {
//                Biome mobspawnerbase = biomes[k + l * 16];
                boolean flag = sandNoise[k + l * 16] + chunkRandom.nextDouble() * 0.20000000000000001D > 0.0D;
                boolean flag1 = gravelNoise[k + l * 16] + chunkRandom.nextDouble() * 0.20000000000000001D > 3D;
                int i1 = (int) (stoneNoise[k + l * 16] / 3D + 3D + chunkRandom.nextDouble() * 0.25D);
                int j1 = -1;
                Block topBlock = blocks.grass; // TODO: Biome Top block
                Block fillerBlock = blocks.dirt; // TODO: Filler block
                for (int k1 = 127; k1 >= 0; k1--) {
                    int l1 = (l * 16 + k) * 128 + k1;
                    if (k1 <= chunkRandom.nextInt(5)) {
                        blocksFlat[l1] = blocks.bedrock;
                        continue;
                    }
                    Block block = blocksFlat[l1];
                    if (block == null) {
                        j1 = -1;
                        continue;
                    }
                    if (block != blocks.stone) {
                        continue;
                    }
                    if (j1 == -1) {
                        if (i1 <= 0) {
                            topBlock = null;
                            fillerBlock = blocks.stone;
                        } else if (k1 >= seaLevel - 4 && k1 <= seaLevel + 1) {
                            topBlock = blocks.grass; //TODO: Biome Top block
                            fillerBlock = blocks.dirt; // TODO: Filler block
                            if (flag1) {
                                topBlock = null;
                            }
                            if (flag1) {
                                fillerBlock = blocks.stone; // TODO: REPLACE WITH GRAVEL
                            }
                            if (flag) {
                                topBlock = blocks.dirt; // TODO: REPLACE WITH SAND
                            }
                            if (flag) {
                                fillerBlock = blocks.dirt; // TODO: REPLACE WITH SAND
                            }
                        }
                        if (k1 < seaLevel && topBlock == null) {
                            topBlock = null; //TODO: REPLACE WITH WATER
                        }
                        j1 = i1;
                        if (k1 >= seaLevel - 1) {
                            blocksFlat[l1] = topBlock;
                        } else {
                            blocksFlat[l1] = fillerBlock;
                        }
                        continue;
                    }
                    if (j1 <= 0) {
                        continue;
                    }
                    j1--;
                    blocksFlat[l1] = fillerBlock;
                    //TODO: ADD SANDSTONE
//                    if (j1 == 0 && fillerBlock == Block.sand.blockID) {
//                        j1 = chunkRandom.nextInt(4);
//                        fillerBlock = (byte) Block.sandStone.blockID;
//                    }
                }

            }

        }
    }

}
