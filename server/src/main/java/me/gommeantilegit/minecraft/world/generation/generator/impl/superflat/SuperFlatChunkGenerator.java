package me.gommeantilegit.minecraft.world.generation.generator.impl.superflat;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.block.ServerBlock;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
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

    @NotNull
    private final ServerMinecraft mc;

    public SuperFlatChunkGenerator(@NotNull WorldGenerator worldGenerator, @Nullable ServerMinecraft mc) {
        super(worldGenerator);
        this.villageGenerator = worldGenerator.getWorldGenerationOptions().isVillages() ? new VillageGenerator(worldGenerator) : null;
        this.mc = mc;
    }

    @Override
    public void onChunkCreated(@NotNull ServerChunk chunk) {
        for (int y = 0; y < 4; y++) {
            for (int xo = 0; xo < ChunkBase.CHUNK_SIZE; xo++) {
                for (int zo = 0; zo < ChunkBase.CHUNK_SIZE; zo++) {
                    int x = chunk.getX() + xo;
                    int z = chunk.getZ() + zo;
                    ServerBlock block;
                    switch (y) {
                        case 0:
                            block = mc.blocks.bedrock;
                            break;
                        case 1:
                        case 2:
                            block = mc.blocks.dirt;
                            break;
                        case 3:
                            block = mc.blocks.grass;
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
