package me.gommeantilegit.minecraft.world.generation.generator.options;

import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTBoolean;
import me.gommeantilegit.minecraft.nbt.impl.NBTStringMap;
import org.jetbrains.annotations.NotNull;

public class WorldGenerationOptions {

    @NotNull
    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

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


    public static class NBTConverter implements INBTConverter<NBTStringMap, WorldGenerationOptions> {

        /**
         * The Key under which the boolean state of {@link WorldGenerationOptions#villages} is stores in a representing {@link NBTStringMap}. see
         */
        private static final String VILLAGES_KEY = "vl";

        @NotNull
        @Override
        public NBTStringMap toNBTData(WorldGenerationOptions object) {
            NBTStringMap map = new NBTStringMap();
            map.put(VILLAGES_KEY, new NBTBoolean(object.isVillages()));
            return map;
        }

        @NotNull
        @Override
        public WorldGenerationOptions fromNBTData(NBTStringMap object, Object... args) throws NBTParsingException {
            NBTBoolean nbtBoolean = (NBTBoolean) object.get(VILLAGES_KEY);
            if (nbtBoolean == null) {
                throw new NBTParsingException("Cannot find state of villages under key \"" + VILLAGES_KEY + "\"");
            }
            return new WorldGenerationOptions(nbtBoolean.getValue());
        }
    }

}
