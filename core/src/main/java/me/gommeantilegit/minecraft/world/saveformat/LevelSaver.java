package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.writer.NBTStreamWriter;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.saveformat.components.SavedWorldState;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;

public class LevelSaver {

    /**
     * World to be saved
     */
    @NotNull
    private final World world;

    /**
     * @param world sets {@link #world}
     */
    public LevelSaver(@NotNull World world) {
        this.world = world;
    }

    /**
     * Writes the data representing {@link #world} to the output stream
     *
     * @param outputStream the output stream
     * @return this
     */
    @NotNull
    public LevelSaver save(@NotNull DataOutputStream outputStream) throws IOException {
        NBTArray nbtData = toNBTArray();
        NBTStreamWriter nbtWriter = new NBTStreamWriter(outputStream);
        nbtWriter.write(nbtData);
        nbtWriter.getDataOutputStream().close();
        return this;
    }

    @NotNull
    private NBTArray toNBTArray() {
        return SavedWorldState.NBT_CONVERTER.toNBTData(new SavedWorldState(world));
    }

    @NotNull
    public World getWorld() {
        return world;
    }
}
