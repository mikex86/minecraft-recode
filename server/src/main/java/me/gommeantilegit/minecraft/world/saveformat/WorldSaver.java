package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class WorldSaver {

    /**
     * ServerWorld to be saved
     */
    @NotNull
    private final ServerWorld world;

    /**
     * The world directory to save the world into
     */
    @NotNull
    private final File worldDirectory;

    /**
     * @param world sets {@link #world}
     */
    public WorldSaver(@NotNull ServerWorld world, @NotNull File worldDirectory) {
        this.world = world;
        this.worldDirectory = worldDirectory;
    }

    @NotNull
    public ServerWorld getWorld() {
        return world;
    }

    public void saveAllChunks() {

    }
}
