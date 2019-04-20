package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

public class ClientChunkCreator extends ChunkCreatorBase {

    @NotNull
    private final ClientMinecraft mc;

    public ClientChunkCreator(@NotNull ClientMinecraft mc, @NotNull ClientWorld world) {
        super(world);
        this.mc = mc;
    }

    @Override
    protected void addChunk(int chunkX, int chunkZ, WorldBase world) {
        addChunk(new ClientChunk(world.height, chunkX, chunkZ, (ClientWorld) world, mc));
    }

    @Override
    protected int getChunkLoadingDistance(@NotNull Entity ent) {
        return world.getChunkLoadingDistance();
    }

}
