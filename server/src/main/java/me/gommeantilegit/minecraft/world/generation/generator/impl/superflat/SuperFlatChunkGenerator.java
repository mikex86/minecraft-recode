package me.gommeantilegit.minecraft.world.generation.generator.impl.superflat;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class SuperFlatChunkGenerator extends ChunkGenerator {

    @NotNull
    private final ServerMinecraft mc;

    public SuperFlatChunkGenerator(@NotNull WorldGenerator worldGenerator, @NotNull ServerMinecraft mc) {
        super(worldGenerator);
        this.mc = mc;
    }

    @Override
    public void onChunkCreated(@NotNull ChunkBase chunk) {
        for (int y = 0; y < 4; y++) {
            for (int xo = 0; xo < ChunkBase.CHUNK_SIZE; xo++) {
                for (int zo = 0; zo < ChunkBase.CHUNK_SIZE; zo++) {
                    int x = chunk.getX() + xo;
                    int z = chunk.getZ() + zo;
                    Block block;
                    switch (y) {
                        case 0:
                            block = mc.getBlocks().bedrock;
                            break;
                        case 1:
                        case 2:
                            block = mc.getBlocks().dirt;
                            break;
                        case 3:
                            block = mc.getBlocks().grass;
                            break;
                        default:
                            block = null;
                            break;
                    }
                    chunk.setBlock(x, y, z, block == null ? null : block.getDefaultBlockState());
                }
            }
        }
    }
}
