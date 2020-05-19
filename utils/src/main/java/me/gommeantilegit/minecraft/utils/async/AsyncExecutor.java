
package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * Allows asnynchronous execution of {@link AsyncTask} instances on a separate thread. Needs to be disposed via a call to
 * {@link #dispose()} when no longer used, in which case the executor waits for running tasks to finish. Scheduled but not yet
 * running tasks will not be executed.
 *
 * @author badlogic
 */
public class AsyncExecutor {

    @NotNull
    private final ListenableThreadPoolExecutor executor;

    /**
     * Creates a new AsynchExecutor that allows maxConcurrent {@link Runnable} instances to run in parallel.
     *
     * @param maxConcurrent
     */
    public AsyncExecutor(int maxConcurrent) {
        executor = new ListenableThreadPoolExecutor(maxConcurrent, maxConcurrent,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r, "AsyncExecutor-Thread");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
            return thread;
        });
    }

    /**
     * Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
     * be queued.
     *
     * @param task the task to execute asynchronously
     */
    public <T> AsyncResult<T> submit(final @NotNull AsyncTask<T> task) {
        if (executor.isShutdown()) {
            throw new RuntimeException("Cannot run tasks on an executor that has been shutdown (disposed)");
        }
        return new AsyncResult<>(executor.submit(task::call));
    }

    /**
     * Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
     * be queued.
     *
     * @param task the task to execute asynchronously
     */
    public <T> AsyncResult<T> submit(final @NotNull AsyncTask<T> task, @NotNull Invokable<ListenableFuture<T>> invokable) {
        if (executor.isShutdown()) {
            throw new RuntimeException("Cannot run tasks on an executor that has been shutdown (disposed)");
        }
        ListenableFuture<T> future = executor.submit(task::call, invokable);
        return new AsyncResult<>(future);
    }

    /**
     * Submits a {@link Runnable} to be executed asynchronously. If maxConcurrent runnables are already running, the runnable will
     * be queued.
     *
     * @param task  the task to execute asynchronously
     * @param delay the delay until the task should be executed in milliseconds
     */
    public <T> AsyncResult<T> submit(final @NotNull AsyncTask<T> task, long delay) {
        if (executor.isShutdown()) {
            throw new RuntimeException("Cannot run tasks on an executor that has been shutdown (disposed)");
        }
        return new AsyncResult<>(executor.submit(task::call));
    }

    /**
     * Waits for running {@link AsyncTask} instances to finish, then destroys any resources like threads. Can not be used after
     * this method is called.
     */
    public void dispose() {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Couldn't shutdown loading thread", e);
        }
    }
}
