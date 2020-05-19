package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import org.jetbrains.annotations.Nullable;

public class ServerWorldChunkHandler extends WorldChunkHandlerBase {

    public ServerWorldChunkHandler() {
        super();
    }

    @Nullable
    @Override
    public ServerChunk getChunkAt(int originX, int originZ) {
        return (ServerChunk) super.getChunkAt(originX, originZ);
    }

}
