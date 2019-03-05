package me.gommeantilegit.minecraft.world.renderer.graphics;

import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.renderer.graphics.sky.Sky;
import org.jetbrains.annotations.NotNull;

public class Graphics {

    @NotNull
    private final Sky sky;

    public Graphics(@NotNull World world) {
        this.sky = new Sky(world);
    }

    public void apply() {
        this.sky.apply();
    }

    @NotNull
    public Sky getSky() {
        return sky;
    }
}
