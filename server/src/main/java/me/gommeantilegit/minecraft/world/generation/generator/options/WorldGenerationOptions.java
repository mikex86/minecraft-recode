package me.gommeantilegit.minecraft.world.generation.generator.options;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public class WorldGenerationOptions {

    /**
     * State whether or not villages should be generated
     */
    @SerializedName("generate_villages")
    private final boolean villages;

    /**
     * The type of the world
     */
    @NotNull
    @SerializedName("world_type")
    private final WorldType worldType;

    /**
     * The randomizer seed of the world
     */
    @SerializedName("world_seed")
    private final long seed;

    public WorldGenerationOptions(long seed, @NotNull WorldType worldType, boolean villages) {
        this.seed = seed;
        this.worldType = worldType;
        this.villages = villages;
    }

    public long getSeed() {
        return seed;
    }

    @NotNull
    public WorldType getWorldType() {
        return worldType;
    }

    public boolean spawnVillages() {
        return villages;
    }

    public enum WorldType {
        OVERWORLD, SUPER_FLAT
    }
}
