package me.gommeantilegit.minecraft.world.chunk.creator;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.math.vecmath.intvectors.Vec2i;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.world.WorldChunkHandlerBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;

/**
 * Object generating chunks around entities and handling the access to the chunk array lists
 */
public abstract class ChunkCreatorBase implements Tickable {

    /**
     * The parent world
     */
    @NotNull
    protected final WorldBase world;

    public ChunkCreatorBase(@NotNull WorldBase world) {
        this.world = world;
    }

    /**
     * Adds a chunk to the world
     *
     * @param chunkX chunk X origin
     * @param chunkZ chunk Z origin
     * @param world  the world
     */
    @NotNull
    @ThreadSafe
    public ChunkBase createChunk(int chunkX, int chunkZ, WorldBase world) {
        ChunkBase chunk = new ChunkBase(world.getHeight(), chunkX, chunkZ, world);
        createChunk(chunk);
        return chunk;
    }

    @Override
    public void tick(float partialTicks) {
//        for (int i = 0; i < viewers.size(); i++) {
//            Entity ent = viewers.get(i);
//            generateChunksAroundEntity(ent, getChunkLoadingDistance(ent));
//        }
    }

    /**
     * Generates chunks around the entity's position.
     *
     * @param entity      the entity that chunks should be generated around
     * @param maxDistance the maximum distance to a chunk that should be created
     * @return the chunks around the entity (not only the newly created ones)
     */
    @NotNull
    @ThreadSafe
    public List<ChunkBase> generateChunksAroundEntity(@NotNull Entity entity, int maxDistance) {
        return generateChunksAroundPosition(entity.getPositionVector(), maxDistance);
    }

    /**
     * Generates chunks around the specified position.
     *
     * @param positionVector the position that chunks should be generated around
     * @param maxDistance    the maximum distance to a chunk that should be created
     * @return the chunks around the entity (not only the newly created ones)
     */
    @NotNull
    @ThreadSafe
    public List<ChunkBase> generateChunksAroundPosition(@NotNull Vector3 positionVector, int maxDistance) {
        List<ChunkBase> chunkBases = new ArrayList<>();
        int range = (int) ceil(((maxDistance) / (double) ChunkBase.CHUNK_SIZE));

        Vec2i origin = this.world.getChunkOrigin(positionVector.x, positionVector.z);

        // in unit chunks
        int cx = origin.getX() / ChunkBase.CHUNK_SIZE, cz = origin.getY() / ChunkBase.CHUNK_SIZE;
        int cxo = 0, czo = 0;
        int xSize = 1, zSize = 1;

        chunkBases.add(tryCreateChunkFor(origin));

        traverse:
        {
            while (true) {
                for (int i = 0; i < xSize; i++) {
                    cxo++;
                    if (cxo > range) {
                        break traverse;
                    }
                    int chunkX = (cx + cxo) * ChunkBase.CHUNK_SIZE, chunkZ = (cz + czo) * ChunkBase.CHUNK_SIZE;
                    Vec2i vec2i = new Vec2i(chunkX, chunkZ);
                    chunkBases.add(tryCreateChunkFor(vec2i));
                }
                xSize++;
                for (int i = 0; i < zSize; i++) {
                    czo++;
                    if (czo > range) {
                        break traverse;
                    }
                    int chunkX = (cx + cxo) * ChunkBase.CHUNK_SIZE, chunkZ = (cz + czo) * ChunkBase.CHUNK_SIZE;
                    Vec2i vec2i = new Vec2i(chunkX, chunkZ);
                    chunkBases.add(tryCreateChunkFor(vec2i));
                }
                zSize++;

                for (int i = 0; i < xSize; i++) {
                    cxo--;
                    if (cxo < -range) {
                        break traverse;
                    }
                    int chunkX = (cx + cxo) * ChunkBase.CHUNK_SIZE, chunkZ = (cz + czo) * ChunkBase.CHUNK_SIZE;
                    Vec2i vec2i = new Vec2i(chunkX, chunkZ);
                    chunkBases.add(tryCreateChunkFor(vec2i));
                }
                xSize++;
                for (int i = 0; i < zSize; i++) {
                    czo--;
                    if (czo < -range) {
                        break traverse;
                    }
                    int chunkX = (cx + cxo) * ChunkBase.CHUNK_SIZE, chunkZ = (cz + czo) * ChunkBase.CHUNK_SIZE;
                    Vec2i vec2i = new Vec2i(chunkX, chunkZ);
                    chunkBases.add(tryCreateChunkFor(vec2i));
                }
                zSize++;
            }
        }
        return chunkBases;
    }

    /**
     * Defines the chunk that contains the given position vector accordingly to the chunk grid of CHUNK_SIZE by CHUNK_SIZE meaning that chunk origins are always multiples of CHUNK_SIZE. (either positive or negative)
     *
     * @param position the given position vector (2D vector. x represents x axis of world; y represents z axis of world) (Top View)
     * @return the chunk for the specified position. (Either created or existing)
     */
    @NotNull
    @ThreadSafe
    public synchronized ChunkBase tryCreateChunkFor(@NotNull Vec2i position) {
        Vec2i origin = world.getChunkOrigin(position.getX(), position.getY());
        ChunkBase chunk = world.getWorldChunkHandler().getChunkAt(origin.getX(), origin.getY());
        if (chunk == null) {
            return createChunk(origin);
        } else {
            return chunk;
        }
    }

    /**
     * Creates the chunk that contains the given position regardless of whether or not a chunk is already defined for the given region. Invoked by {@link #tryCreateChunkFor(Vec2i)}
     *
     * @param origin the chunk origin where to create the chunk
     */
    @NotNull
    @ThreadSafe
    public synchronized ChunkBase createChunk(@NotNull Vec2i origin) {
        return createChunk(origin.getX(), origin.getY(), world);
    }

    /**
     * Adds the given chunk to the world instance
     *
     * @param chunk the chunk to be added
     */
    @ThreadSafe
    protected synchronized void createChunk(@NotNull ChunkBase chunk) {
        WorldChunkHandlerBase chunkHandler = this.world.getWorldChunkHandler();
        chunkHandler.addChunk(chunk); // Adding the chunk to the world chunks list
        // Chunk is in initial state --> not loaded, not unloaded thus GC loop will not occur
    }
}
