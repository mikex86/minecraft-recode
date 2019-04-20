package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * Represents a ThreadPoolExecutor that you can listen to when it completes tasks by adding listeners
 */
public class ListenableThreadPoolExecutor extends ThreadPoolExecutor {

    public ListenableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ListenableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ListenableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ListenableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public ListenableFuture<Void> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        ListenableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    public <T> ListenableFuture<T> submit(Callable<T> task, Invokable<ListenableFuture<T>> invokable) {
        if (task == null) throw new NullPointerException();
        ListenableFuture<T> ftask = newTaskFor(task);
        invokable.invoke(ftask);
        execute(ftask);
        return ftask;
    }
    @Override
    public <T> ListenableFuture<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        ListenableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    @Override
    protected <T> ListenableFuture<T> newTaskFor(Callable<T> callable) {
        return new ListenableFuture<>(callable);
    }

    @Override
    protected <T> ListenableFuture<T> newTaskFor(@NotNull Runnable runnable, T value) {
        return new ListenableFuture<>(runnable, value);
    }
}
