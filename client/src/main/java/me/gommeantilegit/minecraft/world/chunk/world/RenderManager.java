package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * Object for fluent ChunkBase Iteration for world rendering
 */
@SideOnly(side = Side.CLIENT)
public class RenderManager {

    @NotNull
    private final ClientWorld world;

    /**
     * Index for iteration
     */
    private int iterationIndex = 0;

    /**
     * Current chunks of render queue
     */
    public Iterator<ChunkBase> chunkIterator;

    public RenderManager(@NotNull ClientWorld world) {
        this.world = world;
    }

    public void startStage(@NotNull RenderStage renderStage) {
        List<ChunkBase> chunks;
        switch (renderStage) {
            case CHUNKS:
                chunks = world.getWorldChunkHandler().getLoadedChunks();
                break;
            default:
                return;
        }
        this.chunkIterator = chunks.iterator();
        this.iterationIndex = 0;
    }

    public boolean hasNext() {
        return this.chunkIterator.hasNext();
    }

    @NotNull
    public ClientChunk nextChunk() {
        return (ClientChunk) this.chunkIterator.next();
    }

//    @NotNull
//    private final Clock uploadClock = new Clock(false);
//
    public boolean canUpload() {
        return true;
    }

    /**
     * Render state enum
     */
    public enum RenderStage {
        CHUNKS
    }
}
