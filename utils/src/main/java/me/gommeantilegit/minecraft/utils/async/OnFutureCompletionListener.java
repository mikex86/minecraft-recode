package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.Nullable;

public interface OnFutureCompletionListener<T> {

    /**
     * Invoked when the parent future is completed
     * @param t the value of completion
     */
    void onFutureCompleted(@Nullable T t);

}
