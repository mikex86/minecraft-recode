package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PausableExecutor extends ThreadPoolExecutor implements ExecutorService {

    private boolean isPaused = false;

    @NotNull
    private final Lock pauseLock = new ReentrantLock();

    @NotNull
    private Condition unpaused = pauseLock.newCondition();

    @NotNull
    private final List<Runnable> runningTasks = Collections.synchronizedList(new ArrayList<>());

    @Nullable
    private CountDownLatch countDownLatch = null;

    public PausableExecutor(int nThreads, @NotNull ThreadFactory threadFactory) {
        super(nThreads, nThreads, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), threadFactory);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) {
                unpaused.await();
            }
        } catch (InterruptedException e) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
        runningTasks.add(r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        runningTasks.remove(r);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        super.execute(command);
    }

    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
            countDownLatch = new CountDownLatch(this.runningTasks.size());
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } finally {
            pauseLock.unlock();
        }
    }

    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            countDownLatch = null;
            unpaused.signal();
        } finally {
            pauseLock.unlock();
        }
    }
}