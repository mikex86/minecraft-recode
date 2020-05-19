package me.gommeantilegit.minecraft.utils.async;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Represents a thread on which tasks can be schedules
 */
public class SchedulableThread extends Thread {

    /**
     * Queue of Runnables to be executed on the thread
     */
    @NotNull
    private final Queue<Runnable> tasks = new LinkedBlockingDeque<>();

    public SchedulableThread() {
    }

    public SchedulableThread(Runnable target) {
        super(target);
    }

    public SchedulableThread(@Nullable ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public SchedulableThread(@NotNull String name) {
        super(name);
    }

    public SchedulableThread(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    public SchedulableThread(Runnable target, String name) {
        super(target, name);
    }

    public SchedulableThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name) {
        super(group, target, name);
    }

    public SchedulableThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    /**
     * Executes and removes all queued tasks of {@link #tasks}
     */
    protected void updateTasks() {
        synchronized (this.tasks) {
            while (!tasks.isEmpty()) {
                Runnable remove = tasks.remove();
                remove.run();
            }
        }
    }

    /**
     * Schedules the runnable to be executed on the thread
     *
     * @param runnable the runnable to be executed
     */
    public void scheduleTask(@NotNull Runnable runnable) {
        synchronized (this.tasks) {
            this.tasks.add(runnable);
        }
    }

    @NotNull
    public Queue<Runnable> getTasks() {
        return tasks;
    }
}
