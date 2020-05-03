package me.gommeantilegit.minecraft.world.chunk.creator;

import me.gommeantilegit.minecraft.annotations.Unsafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import org.jetbrains.annotations.NotNull;

public class ServerChunkCreator extends ChunkCreatorBase {

    public ServerChunkCreator(@NotNull ServerWorld world) {
        super(world);
    }

    /**
     * Adds the given chunk to the world instance.
     * - Applies block state from saved world instance
     * - Invokes the creator listener of {@link #world}
     * - Adds the instance to {@link WorldChunkHandlerBase#collectChunks()} of {@link ServerWorld#getWorldChunkHandler()} of {@link #world}
     * <p><br>
     * NOTE: The listener is invoked before the chunk is added to the list. (If the listener is present meaning the field is not null)<br>
     * ALSO NOTE: USE MINECRAFT THREAD ONLY<br>
     *
     * @param chunk the chunk to be added
     * @see OnChunkCreationListener
     */
    @Unsafe
    protected void createChunk(@NotNull ChunkBase chunk) {
        if (!this.world.getOnChunkCreationListeners().isEmpty()) {
            this.world.getOnChunkCreationListeners().forEach(l -> l.onChunkCreated(chunk));
        }

        // IMPORTANT: Invoke terrain generation before adding it, because whether a chunk exists at a given origin is used to determine whether terrain generation should be invoked
        if (((ServerWorld) world).getInvokeTerrainGenerationDecider().shouldInvokeTerrainGeneration(world, chunk))
            ((ServerWorld) world).getWorldGenerator().onChunkCreated(chunk); // Invoking the terrain generation
        super.createChunk(chunk);
    }
}
