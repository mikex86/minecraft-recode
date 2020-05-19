package me.gommeantilegit.minecraft.world.chunk;

import me.gommeantilegit.minecraft.block.state.palette.IBlockStatePalette;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;

public class ServerChunk extends ChunkBase {

    private boolean worldGenerationFinished = false;

    public ServerChunk(int height, int x, int z, @NotNull WorldBase world, @NotNull IBlockStatePalette blockStatePalette) {
        super(height, x, z, world, blockStatePalette);
    }

    public void setWorldGenerationFinished(boolean worldGenerationFinished) {
        this.worldGenerationFinished = worldGenerationFinished;
    }

    public boolean isWorldGenerationFinished() {
        return worldGenerationFinished;
    }
}
