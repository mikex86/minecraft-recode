package me.gommeantilegit.minecraft.world.chunk.builder;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import kotlin.Pair;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.timer.api.AbstractAsyncOperation;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

@SideOnly(side = Side.CLIENT)
public class ChunkRebuilder extends AbstractAsyncOperation implements OpenGLOperation {

    /**
     * Parent world
     */
    @NotNull
    private final ClientWorld world;

    /**
     * Queue containing all chunks with unfinished meshes with their builder as it's parent
     */
    @NotNull
    private final Queue<Pair<ClientChunk, MeshBuilder>> unfinishedMeshes = new LinkedList<>();

    /**
     * Queue of chunks that need to be rebuilt
     */
    @NotNull
    private final LinkedList<ClientChunk> rebuildQueue = new LinkedList<>();

    /**
     * State whether chunk meshes need to be finished on Game Tick
     */
    private boolean finishMeshes;

    /**
     * State whether chunks need to be rebuilt
     */
    private boolean chunksToRebuild;

    /**
     * @param world sets {@link #world}
     */
    public ChunkRebuilder(@NotNull ClientWorld world) {
        super("ChunkRebuilder-AsyncThread", 20);
        this.world = world;
    }

    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
        if (finishMeshes) {
            while (!unfinishedMeshes.isEmpty()) {
                Pair<ClientChunk, MeshBuilder> pair;
                try {
                    synchronized (unfinishedMeshes) {
                        pair = unfinishedMeshes.remove();
                    }
                    ClientChunk chunk = pair.getFirst();
                    MeshBuilder meshBuilder = pair.getSecond();
                    if (chunk.getMesh() != null)
                        chunk.getMesh().dispose();
                    chunk.setMesh(meshBuilder.end());
                } catch (NoSuchElementException ignored) {
                }
            }
            finishMeshes = false;

        }
    }

    @Override
    public void onAsyncThread() {
        try {
            if (chunksToRebuild) {
                while (!this.rebuildQueue.isEmpty()) {
                    ClientChunk chunk;
                    synchronized (rebuildQueue) {
                        chunk = this.rebuildQueue.removeLast();
                    }
                    if (chunk.needsRebuild()) {
                        MeshBuilder meshBuilder = chunk.buildChunkMesh();
                        synchronized (unfinishedMeshes) {
                            this.unfinishedMeshes.add(new Pair<>(chunk, meshBuilder));
                            chunk.setNeedsRebuild(false, false);
                            finishMeshes = true;
                        }
                    }

                    chunksToRebuild = false;

                }
            }
        } catch (NoSuchElementException ignored) {
        }
    }

    /**
     * Schedules the given chunk to have it's mesh rebuilt
     *
     * @param clientChunk  the chunk that needs it's mesh to be rebuilt
     * @param highPriority state whether the chunk should be rebuilt with high priority meaning it is inserted as the first chunk to be rebuilt in the rebuild queue
     */
    public void scheduleRebuild(@NotNull ClientChunk clientChunk, boolean highPriority) {
        synchronized (rebuildQueue) {
            if (highPriority)
                this.rebuildQueue.addLast(clientChunk); // The first chunk to be removed and thus rebuilt is the last element of the Queue
            else
                this.rebuildQueue.addFirst(clientChunk);
            this.chunksToRebuild = true;
        }
    }
}
