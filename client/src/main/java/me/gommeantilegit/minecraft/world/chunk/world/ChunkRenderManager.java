package me.gommeantilegit.minecraft.world.chunk.world;

import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Object for fluent ChunkBase Iteration for world rendering
 */
@SideOnly(side = Side.CLIENT)
public class ChunkRenderManager {

    @NotNull
    private final ClientWorld world;

    /**
     * Index for iteration
     */
    private int iterationIndex = 0;

    /**
     * Current chunks of render queue
     */
    public List<ChunkBase> chunks;

    public ChunkRenderManager(@NotNull ClientWorld world) {
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
        this.chunks = chunks;
        iterationIndex = 0;
    }

    public boolean hasNext() {
        return iterationIndex < chunks.size();
    }

    public ClientChunk nextChunk() {
        try {
            return (ClientChunk) this.chunks.get(iterationIndex++);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Render state enum
     */
    public enum RenderStage {
        CHUNKS
    }
}
