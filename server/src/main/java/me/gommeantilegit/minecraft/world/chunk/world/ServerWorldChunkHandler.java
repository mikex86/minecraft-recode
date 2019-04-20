package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.block.ServerBlock;
import me.gommeantilegit.minecraft.block.ServerBlocks;
import me.gommeantilegit.minecraft.block.state.ServerBlockState;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import org.jetbrains.annotations.NotNull;

public class ServerWorldChunkHandler extends WorldChunkHandlerBase<ServerChunk, ServerMinecraft, ServerBlock, ServerBlocks, ServerBlockState> {

    public ServerWorldChunkHandler(@NotNull ServerMinecraft serverMinecraft) {
        super();
    }
}
