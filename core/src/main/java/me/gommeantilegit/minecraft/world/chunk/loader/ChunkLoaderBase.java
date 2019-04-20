package me.gommeantilegit.minecraft.world.chunk.loader;

import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class ChunkLoaderBase<CB extends ChunkBase<?, CB, ?, ?, ?, ?, ?, WB, ?>, WB extends WorldBase<?, ?, ?, WB, ?, CB, ?, ?, ?>> implements AsyncOperation {

    /**
     * Parent world object
     */
    @NotNull
    protected final WB world;
    /**
     * @param world sets {@link #world}
     */
    public ChunkLoaderBase(@NotNull WB world) {
        this.world = world;
    }

    @Override
    public void onAsyncThread() {
    }
}
