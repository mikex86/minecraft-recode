package me.gommeantilegit.minecraft.world.generation.generator.villages.generator;

import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.api.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class VillageGenerator extends ChunkGenerator {

    @NotNull
    private final VillagesDistribution villagesDistribution;

    public VillageGenerator(@NotNull WorldGenerator worldGenerator) {
        super(worldGenerator);
        this.villagesDistribution = new VillagesDistribution(worldGenerator.getRandom());
    }

    /**
     * Generating villages
     * @param chunk the chunk, that is added
     */
    @Override
    public void onChunkCreated(@NotNull ServerChunk chunk) {

    }
}
