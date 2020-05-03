package me.gommeantilegit.minecraft.world.entity;

import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Spawns entities into the world and handles the chunk creation for entity spawning
 */
public class EntitySpawner {

    /**
     * The parent world
     */
    @NotNull
    private final WorldBase world;

    public EntitySpawner(@NotNull WorldBase world) {
        this.world = world;
    }

    /**
     * Creates the needed chunk for the given entity and assigns it to it's chunk.
     *
     * @param entity the given entity
     */
    @ThreadSafe
    private void handle(@NotNull Entity entity) {
        int chunkLoadingDistance = world.getChunkLoader().getChunkLoadingDistance(entity);
        ChunkBase chunk = this.world.getChunkFor(entity);
        if (chunk == null) {
            this.world.getChunkCreator().generateChunksAroundEntity(entity, chunkLoadingDistance); // could be asynchronous
            chunk = this.world.getChunkFor(entity);
            Objects.requireNonNull(chunk, "Chunk was not created after explicitly requested");
        }
        entity.onSpawned(chunk);
        chunk.scheduleAddEntity(entity);
    }

    /**
     * Spawns the entity into the world. Chunks at the initial entity position will be created, but not necessarily loaded
     * @param entity the given entity
     */
    @ThreadSafe
    public void spawnEntity(@NotNull Entity entity) {
        this.handle(entity);
    }
}
