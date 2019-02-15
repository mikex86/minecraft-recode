package me.gommeantilegit.minecraft.world.chunk.builder;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;

import kotlin.Pair;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.util.collections.ThreadBoundList;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static java.lang.StrictMath.hypot;

public class ChunkRebuilder implements OpenGLOperation, AsyncOperation {

    /**
     * Parent world
     */
    @NotNull
    private final World world;

    /**
     * Queue containing all chunks with unfinished meshes with their builder as it's parent
     */
    @NotNull
    private final Queue<Pair<Chunk, MeshBuilder>> unfinishedMeshes = new LinkedList<>();

    /**
     * State if Chunk meshes need to be finished on Game Tick
     */
    private boolean finishMeshes;

    /**
     * @param world sets {@link #world}
     */
    public ChunkRebuilder(@NotNull World world) {
        this.world = world;
    }

    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
        if(finishMeshes) {
            while (!unfinishedMeshes.isEmpty()) {
                Pair<Chunk, MeshBuilder> pair;
                try {
                    pair = unfinishedMeshes.remove();
                    Chunk chunk = pair.getFirst();
                    MeshBuilder meshBuilder = pair.getSecond();
                    Mesh chunkMesh = meshBuilder.end();
                    chunk.setMesh(chunkMesh);
                } catch (NoSuchElementException ignored) {
                }
            }
            finishMeshes = false;
        }
    }

    @Override
    public void onAsyncThread() {
        try {
            ThreadBoundList<Chunk> chunks = world.getWorldChunkHandler().getChunks();
            for (int i = 0; i < chunks.size(); i++) {
                Chunk chunk = chunks.get(i);
                if (chunk.needsRebuild()) {
                    MeshBuilder meshBuilder = chunk.buildChunkMesh();
                    this.unfinishedMeshes.add(new Pair<>(chunk, meshBuilder));
                    chunk.setNeedsRebuild(false);
                    finishMeshes = true;
                }
            }
        } catch (ConcurrentModificationException e) {
        }
    }
}
