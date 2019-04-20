package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

public class ClientChunkCreator extends ChunkCreatorBase<ClientWorld, ClientChunk> {

    public ClientChunkCreator(@NotNull ClientWorld world) {
        super(world);
    }

    @Override
    protected void addChunk(int chunkX, int chunkZ, ClientWorld world) {
        addChunk(new ClientChunk(world.height, chunkX, chunkZ, world));
    }


    @Override
    protected void addChunk(@NotNull ClientChunk chunk) {
        super.addChunk(chunk);
    }

    @Override
    protected int getChunkLoadingDistance(@NotNull Entity ent) {
        return world.getChunkLoadingDistance();
    }

}
