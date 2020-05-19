package me.gommeantilegit.minecraft.utils.async;

public interface FailingRunnable<T extends Throwable> {

    void run() throws T;

}
