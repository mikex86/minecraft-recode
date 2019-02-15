package me.gommeantilegit.minecraft.timer.tick;

import me.gommeantilegit.minecraft.Minecraft;
import org.jetbrains.annotations.NotNull;

public class MinecraftThread extends Thread {

    @NotNull
    private final Minecraft mc;

    public MinecraftThread(@NotNull Minecraft mc) {
        super("Minecraft-Thread");
        this.mc = mc;
    }

    @Override
    public void run() {
        try {
            while (mc.isRunning()) {
                //Updating timer and ticks
                {
                    mc.timer.advanceTime();
                    for (int i = 0; i < mc.timer.ticks; i++) {
                        mc.tick(mc.timer.partialTicks);
                        mc.timer.tick(mc.timer.partialTicks);
                    }
                }
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
