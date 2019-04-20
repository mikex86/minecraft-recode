package me.gommeantilegit.minecraft.timer.api;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a repeating async operation performed on a separate thread
 */
public abstract class AbstractAsyncOperation implements AsyncOperation {

    @NotNull
    private final Thread asyncThread;

    protected AbstractAsyncOperation(@NotNull String threadName, long sleepMillis) {
        this.asyncThread = new Thread(() -> {
            try {
                while (true) {
                    onAsyncThread();
                    Thread.sleep(sleepMillis);
                }
            } catch (InterruptedException ignored) {
            }
        });
        this.asyncThread.setName(threadName);
        this.asyncThread.setDaemon(true);
        this.asyncThread.start();
    }


    /**
     * Stops all async work performed by the object
     */
    @Override
    public void stopAsyncWork() {
        this.asyncThread.interrupt();
    }
}
