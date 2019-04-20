package me.gommeantilegit.minecraft.world.chunk;

import org.jetbrains.annotations.NotNull;

public class ServerChunkSection extends ChunkSection<ServerChunk> {

    public ServerChunkSection(@NotNull ServerChunk parentChunk, int startHeight) {
        super(parentChunk, startHeight);
    }

}
