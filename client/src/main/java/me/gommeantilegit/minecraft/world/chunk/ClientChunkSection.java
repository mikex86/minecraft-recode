package me.gommeantilegit.minecraft.world.chunk;

import org.jetbrains.annotations.NotNull;

public class ClientChunkSection extends ChunkSection<ClientChunk> {

    public ClientChunkSection(@NotNull ClientChunk parentChunk, int startHeight) {
        super(parentChunk, startHeight);
    }
}
