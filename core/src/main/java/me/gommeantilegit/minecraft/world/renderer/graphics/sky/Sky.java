package me.gommeantilegit.minecraft.world.renderer.graphics.sky;

import me.gommeantilegit.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the sky
 */
public class Sky {

    /**
     * Sun instance of the sky
     */
    @NotNull
    private final Sun sun;

    @NotNull
    private final World world;

    public Sky(@NotNull World world){
        this.sun = new Sun(world);
        this.world = world;
    }

    public void apply(){
        this.world.spawnEntityInWorld(sun);
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    public Sun getSun() {
        return sun;
    }
}
