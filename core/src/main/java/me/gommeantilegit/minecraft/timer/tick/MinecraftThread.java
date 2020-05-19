package me.gommeantilegit.minecraft.timer.tick;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.utils.async.SchedulableThread;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.LockSupport;

public class MinecraftThread extends SchedulableThread {

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final AbstractMinecraft mc;

    /**
     * The amount of idle ticks per second should be performed to remain at a steady timer tick speed.
     * Increasing this value will reduce fluctuation of the tps value while most likely increasing cpu usage.
     */
    private final int idleTicks;

    public MinecraftThread(@NotNull AbstractMinecraft mc) {
        this(mc, 100);
    }

    public MinecraftThread(@NotNull AbstractMinecraft mc, int idleTicks) {
        super("Minecraft-Thread");
        this.mc = mc;
        this.idleTicks = idleTicks;
        this.setDaemon(true);
        this.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        try {
            while (mc.isRunning()) {
                long start = System.nanoTime();
                onUpdate();
                long end = System.nanoTime();
                LockSupport.parkNanos((long) (1_000_000_000 / (mc.getTimer().getTicksPerSecond() + idleTicks)) - (end - start));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onInterrupted() {

    }

    /**
     * Called to update the game logic
     */
    public void onUpdate() {
        updateTasks();
        mc.onUpdate();
    }
}
