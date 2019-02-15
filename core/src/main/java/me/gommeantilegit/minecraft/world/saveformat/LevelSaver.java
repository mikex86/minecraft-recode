package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTInteger;
import me.gommeantilegit.minecraft.nbt.writer.NBTStreamWriter;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.block.change.BlockChange;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        return this;
    }

    @NotNull
    private NBTArray toNBTArray() {
        List<NBTObject<?>> objects = new ArrayList<>();

        objects.add(new NBTInteger(world.getWorldGenerator().getSeed())); // Writing seed
        objects.add(new NBTInteger(world.getWorldGenerator().getWorldType().ordinal())); // Writing worldType
        objects.add(world.getWorldGenerator().getWorldGenerationOptions().toNBTStringMap());

        // Writing Block changes
        List<BlockChange> blockChanges = world.getBlockChanges();
        List<NBTArray> nbtBlockChanges = new ArrayList<>();

        for (BlockChange change : blockChanges) {
            nbtBlockChanges.add(new NBTArray(new NBTObject[]{
                    new NBTInteger(change.getPosition().getX()),
                    new NBTInteger(change.getPosition().getY()),
                    new NBTInteger(change.getPosition().getZ()),
                    BlockState.NBT_CONVERTER.toNBTData(change.getNewBlockState()),
                    BlockState.NBT_CONVERTER.toNBTData(change.getOriginalBlockState())
            }));
        }

        objects.add(new NBTArray(nbtBlockChanges.toArray(new NBTArray[0])));
        return new NBTArray(objects.toArray(new NBTObject<?>[0]));
    }

    @NotNull
    public World getWorld() {
        return world;
    }
}
