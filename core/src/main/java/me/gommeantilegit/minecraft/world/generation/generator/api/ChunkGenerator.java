package me.gommeantilegit.minecraft.world.generation.generator.api;

import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import org.jetbrains.annotations.NotNull;

public abstract class ChunkGenerator implements World.OnChunkCreationListener {

    @NotNull
    protected final WorldGenerator worldGenerator;

    public ChunkGenerator(@NotNull WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    @NotNull
    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }
}
