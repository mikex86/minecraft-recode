package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import org.jetbrains.annotations.NotNull;

public class ServerChunkCreator extends ChunkCreatorBase<ServerWorld, ServerChunk> {

    public ServerChunkCreator(@NotNull ServerWorld world) {
        super(world);
    }

    @Override
    protected void addChunk(int chunkX, int chunkZ, ServerWorld world) {
        addChunk(new ServerChunk(world.height, chunkX, chunkZ, world));
    }

    @Override
    protected int getChunkLoadingDistance(@NotNull Entity ent) {
        return ent instanceof EntityPlayerMP ? ((EntityPlayerMP) ent).channelData.getChunkLoadingDistance() : world.getChunkLoadingDistance();
    }

    @Override
    public void onAsyncThread() {
        updatePendingEntities(); // Chunks added on manual request by the client
    }

    /**
     * Adds the given chunk to the world instance.
     * - Applies block state from saved world instance
     * - Invokes the creator listener of {@link #world}
     * - Adds the instance to {@link WorldChunkHandlerBase#chunks} of {@link ServerWorld#worldChunkHandler} of {@link #world}
     * <p><br>
     * NOTE: The listener is invoked before the chunk is added to the list. (If the listener is present meaning the field is not null)<br>
     * ALSO NOTE: USE MINECRAFT THREAD ONLY<br>
     *
     * @param chunk the chunk to be added
     * @see OnChunkCreationListener
     */
    @Unsafe
    protected void addChunk(@NotNull ServerChunk chunk) {
        if (!this.world.getOnChunkCreationListeners().isEmpty()) {
            this.world.getOnChunkCreationListeners().forEach(l -> l.onChunkCreated(chunk));
        }
        if (world.getInvokeTerraionGenerationDecider().invokedTerrainGeneration(chunk))
            world.getWorldGenerator().onChunkCreated(chunk); // Invoking the terrain generation
        super.addChunk(chunk);
    }
}
