package me.gommeantilegit.minecraft.timer.tick;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.utils.async.SchedulableThread;
import org.jetbrains.annotations.NotNull;

public class MinecraftThread extends SchedulableThread {

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final AbstractMinecraft mc;

    /**
     * State whether or not the game ticks should be performed
     */
    private boolean enableMinecraftTick = false;

    public MinecraftThread(@NotNull AbstractMinecraft mc) {
        super("Minecraft-Thread");
        this.mc = mc;
        this.setDaemon(true);
        this.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    }

    @Override
    public void run() {
        try {
            while (mc.isRunning()) {
                long start = System.currentTimeMillis();
                onUpdate();
                long end = System.currentTimeMillis();
                Thread.sleep(Math.max(0, (long) (1000 / mc.getTimer().getTicksPerSecond()) - (end - start)));
            }
        } catch (InterruptedException e) {
            onInterrupted();
        }
    }

    protected void onInterrupted() {

    }

    /**
     * Called to update the game logic
     */
    public void onUpdate() {
        updateTasks();
        //Updating timer and ticks
        if (enableMinecraftTick) {
            mc.getTimer().advanceTime();
            for (int i = 0; i < mc.getTimer().ticks; i++) {
                mc.tick(mc.getTimer().partialTicks);
                mc.getTimer().tick(mc.getTimer().partialTicks);
            }
        }
    }

    /**
     * Activates the repeating tick of the game
     */
    public void startMinecraftGameLogic() {
        this.enableMinecraftTick = true;
    }
}
