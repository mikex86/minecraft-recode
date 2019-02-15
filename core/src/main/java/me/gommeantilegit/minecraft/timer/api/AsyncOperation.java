package me.gommeantilegit.minecraft.timer.api;

public interface AsyncOperation {

    /**
     * Invoked frequently on a async thread
     */
    void onAsyncThread();

}
