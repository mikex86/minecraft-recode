package me.gommeantilegit.minecraft.timer.api;

/**
 * Represents an object updated on timer tick.
 */
public interface Tickable {

    /**
     * Called on timer tick.
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    void tick(float partialTicks);

}
