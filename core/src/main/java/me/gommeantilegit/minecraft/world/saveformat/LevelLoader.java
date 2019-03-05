package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.nbt.NBTStreamReader;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.util.io.IOUtils;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.saveformat.components.SavedWorldState;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class LevelLoader {

    /**
     * Data Input Stream to be read
     */
    @NotNull
    private final DataInputStream dataInputStream;

    /**
     * @param dataInputStream sets {@link #dataInputStream}
     */
    public LevelLoader(@NotNull DataInputStream dataInputStream) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        IOUtils.io(dataInputStream, bytesOut);
        this.dataInputStream = new DataInputStream(new ByteArrayInputStream(bytesOut.toByteArray()));
    }

    /**
     * @param viewer player viewer viewing the world
     * @return the created world instance
     * @throws NBTParsingException if loading fails (World decrypting)
     */
    @NotNull
    public World loadWorld(@NotNull Player viewer) throws NBTParsingException, IOException {
        NBTStreamReader streamReader = new NBTStreamReader(dataInputStream);
        NBTArray array = streamReader.readObject(NBTArray.class);
        SavedWorldState savedWorldState = SavedWorldState.NBT_CONVERTER.fromNBTData(array);
        return new World(viewer,
                new WorldGenerator(
                        savedWorldState.getWorldSeed(),
                        savedWorldState.getWorldType(),
                        savedWorldState.getWorldGenerationOptions()
                ),
                savedWorldState,
                savedWorldState.getWorldHeight());
    }

}
