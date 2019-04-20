package me.gommeantilegit.minecraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A class storing all minecraft instances to be accessible by a given id
 */
public class MinecraftProvider {

    /**
     * The map of minecraft instance parent to their ids
     */
    @NotNull
    private static final HashMap<Long, AbstractMinecraft> MINECRAFT_MAP = new HashMap<>();

    /**
     * Incrementing id for new instances
     */
    private static long id = 0;

    /**
     * Stores ids of minecraft instances that have been removed that can be reused for new minecraft instances
     */
    @NotNull
    private static final Queue<Long> UNUSED_IDS = new LinkedList<>();

    /**
     * Creates a new minecraft instance in {@link #MINECRAFT_MAP} corresponding to the id returned.
     *
     * @param mc the minecraft instance to be stored
     * @return the id of the new stored minecraft instance
     */
    public static long createMinecraft(@NotNull AbstractMinecraft mc) {
        if (!UNUSED_IDS.isEmpty())
            return UNUSED_IDS.remove();
        long id = MinecraftProvider.id;
        MINECRAFT_MAP.put(id, mc);
        MinecraftProvider.id++;
        return id;
    }

    /**
     * Removes the minecraft instance from the provider
     *
     * @param id the id of the minecraft instance to be removed
     */
    public static void removeMinecraft(long id) {
        MINECRAFT_MAP.remove(id);
        UNUSED_IDS.add(id);
    }

    /**
     * @param id the id of the minecraft instance to retrieve
     * @return the minecraft instance parent to the specified id or null if not found
     */
    @Nullable
    public static AbstractMinecraft getMC(long id) {
        return MINECRAFT_MAP.get(id);
    }


}
