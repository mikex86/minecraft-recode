package me.gommeantilegit.minecraft.world.chunk;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.block.ServerBlock;
import me.gommeantilegit.minecraft.block.ServerBlocks;
import me.gommeantilegit.minecraft.block.state.ServerBlockState;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.loader.ServerChunkLoader;
import me.gommeantilegit.minecraft.world.chunk.world.ServerWorldChunkHandler;
import org.jetbrains.annotations.NotNull;

public class ServerChunk extends ChunkBase<ServerChunkSection, ServerChunk, ServerWorldChunkHandler, ServerBlocks, ServerMinecraft, ServerBlock, ServerBlockState, ServerWorld, ServerChunkLoader> {

    /**
     * Default constructor of a ChunkBase object
     *
     * @param height              height of the world -> becomes chunk height
     * @param x                   startX position where the region managed by the chunk starts
     * @param z                   startZ position where the region managed by the chunk starts
     * @param world               the parent world
     */
    public ServerChunk(int height, int x, int z, @NotNull ServerWorld world) {
        super(height, x, z, world, ServerBlockState.class, ServerChunkSection.class);
    }

    @NotNull
    @Override
    protected ServerChunkSection getChunkSection(int startHeight) {
        return new ServerChunkSection(this, startHeight);
    }
}
