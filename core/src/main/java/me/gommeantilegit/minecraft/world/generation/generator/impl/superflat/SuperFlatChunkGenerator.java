package me.gommeantilegit.minecraft.world.generation.generator.impl.superflat;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.villages.generator.VillageGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperFlatChunkGenerator extends ChunkGenerator {

    /**
     * Village generator instance. Null if villages are disable in world creator options.
     */
    @Nullable
    private final VillageGenerator villageGenerator;

    public SuperFlatChunkGenerator(@NotNull WorldGenerator worldGenerator) {
        super(worldGenerator);
        this.villageGenerator = worldGenerator.getWorldGenerationOptions().isVillages() ? new VillageGenerator(worldGenerator) : null;
    }

    @Override
    public void onChunkCreated(@NotNull Chunk chunk) {
        for (int y = 0; y < 4; y++) {
            for (int xo = 0; xo < Chunk.CHUNK_SIZE; xo++) {
                for (int zo = 0; zo < Chunk.CHUNK_SIZE; zo++) {
                    int x = chunk.getX() + xo;
                    int z = chunk.getZ() + zo;
                    Block block;
                    switch (y) {
                        case 0:
                            block = Blocks.BEDROCK;
                            break;
                        case 1:
                        case 2:
                            block = Blocks.DIRT;
                            break;
                        case 3:
                            block = Blocks.GRASS;
                            break;
                        default:
                            block = null;
                            break;
                    }
                    chunk.setBlockNoChange(x, y, z, block == null ? null : block.getDefaultBlockState());
                }
            }
        }
        if (this.villageGenerator != null) {
            this.villageGenerator.onChunkCreated(chunk);
        }
    }
}
