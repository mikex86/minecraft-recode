package me.gommeantilegit.minecraft.world.generation.generator.options;

import me.gommeantilegit.minecraft.nbt.impl.NBTBoolean;
import me.gommeantilegit.minecraft.nbt.impl.NBTStringMap;
import org.jetbrains.annotations.NotNull;

public class WorldGenerationOptions {

    /**
     * The Key under which the boolean state of {@link #villages} is stores in a representing {@link NBTStringMap}. see {@link #toNBTStringMap()}
     */
    public static final String VILLAGES_KEY = "vl";
    /**
     * State whether or not villages should be generated
     */
    private final boolean villages;

    public WorldGenerationOptions(boolean villages) {
        this.villages = villages;
    }

    public boolean isVillages() {
        return villages;
    }

    /**
     * @return an NBTStringMap instance representing this instance
     */
    @NotNull
    public NBTStringMap toNBTStringMap() {
        NBTStringMap map = new NBTStringMap();
        map.put("vl", new NBTBoolean(villages));
        return map;
    }
}
