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
    }

    @Override
    public void run() {
        try {
            while (mc.isRunning()) {
                onUpdate();
                Thread.sleep(10);
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
    protected void onUpdate() {
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
