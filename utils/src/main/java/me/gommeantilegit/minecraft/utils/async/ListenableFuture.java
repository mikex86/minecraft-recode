package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Represents a Future instance that you can listen to when the value is computed
 * @param <T> the type of future
 */
public class ListenableFuture<T> extends FutureTask<T> {

    @NotNull
    private final List<OnFutureCompletionListener<T>> futureCompletionListeners = new ArrayList<>();

    public ListenableFuture(@NotNull Callable<T> callable) {
        super(callable);
    }

    public ListenableFuture(@NotNull Runnable runnable, T result) {
        super(runnable, result);
    }

    @Override
    protected void set(T t) {
        futureCompletionListeners.forEach(l -> l.onFutureCompleted(t));
        super.set(t);
    }

    /**
     * Registers a listener that is invoked when the value is completed
     * @param listener the listener to be registered
     */
    public void registerCompletionListener(@Nullable OnFutureCompletionListener<T> listener){
        this.futureCompletionListeners.add(listener);
    }
}
