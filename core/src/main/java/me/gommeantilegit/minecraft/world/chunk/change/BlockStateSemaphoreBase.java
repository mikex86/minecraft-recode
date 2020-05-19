package me.gommeantilegit.minecraft.world.chunk.change;

import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BlockStateSemaphoreBase implements Tickable {

    @NotNull
    private final ExecutorService service = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "BlockStateSemaphoreBase-Worker");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        return thread;
    });

    @Override
    public void tick(float partialTicks) {
    }

    @NotNull
    public Future<?> writeSynchronized(@NotNull ChunkBase chunk, @NotNull Runnable runnable) {
        return this.service.submit(runnable);
    }
}
