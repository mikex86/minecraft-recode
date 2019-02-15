package me.gommeantilegit.minecraft.world.saveformat;

import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.NBTStreamReader;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.*;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.block.change.BlockChange;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class LevelLoader {

    /**
     * Data Input Stream to be read
     */
    @NotNull
    private final DataInputStream dataInputStream;

    /**
     * @param dataInputStream sets {@link #dataInputStream}
     */
    public LevelLoader(@NotNull DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    /**
     * @param viewer player viewer viewing the world
     * @return the created world instance
     * @throws NBTParsingException if loading fails (World decrypting)
     */
    @NotNull
    public World loadWorld(@NotNull Player viewer) throws NBTParsingException {
        try {
            NBTStreamReader reader = new NBTStreamReader(dataInputStream);
            NBTArray nbtData = reader.readObject(NBTArray.class);
            NBTObject<?>[] value = nbtData.getValue();

            NBTInteger seedElement = (NBTInteger) value[0];
            NBTInteger worldTypeElement = (NBTInteger) value[1];
            NBTStringMap worldGenerationOptionsElement = (NBTStringMap) value[2];
            boolean villages; // State if villages are being generated
            {
                NBTBoolean villagesElement = (NBTBoolean) worldGenerationOptionsElement.get(WorldGenerationOptions.VILLAGES_KEY);
                villages = requireNonNull(villagesElement).getValue();
            }

            WorldGenerationOptions worldGenerationOptions = new WorldGenerationOptions(villages); // World Generation options
            int seed = seedElement.getValue(); // world seed
            int worldTypeOrdinal = worldTypeElement.getValue(); // World type ordinal integer

            WorldGenerator.WorldType worldType = WorldGenerator.WorldType.values()[worldTypeOrdinal]; // World Type Enum Value

            List<BlockChange> worldBlockChanges = new ArrayList<>();
            {
                NBTArray blockChangesElement = (NBTArray) value[3];
                NBTObject<?>[] blockChanges = blockChangesElement.getValue();
                for (NBTObject blockChangeArray : blockChanges) {
                    NBTObject<?>[] blockChange = ((NBTArray) blockChangeArray).getValue();
                    BlockChange blockChangeInstance;
                    {
                        NBTInteger nbtPosX = (NBTInteger) blockChange[0];
                        NBTInteger nbtPosY = (NBTInteger) blockChange[1];
                        NBTInteger nbtPosZ = (NBTInteger) blockChange[2];
                        NBTArray nbtNewBlockState = (NBTArray) blockChange[3];
                        NBTArray nbtOriginalBlockState = (NBTArray) blockChange[4];
                        blockChangeInstance = new BlockChange(new BlockPos(nbtPosX.getValue(), nbtPosY.getValue(), nbtPosZ.getValue()), BlockState.NBT_CONVERTER.fromNBTData(nbtNewBlockState), BlockState.NBT_CONVERTER.fromNBTData(nbtOriginalBlockState));
                        worldBlockChanges.add(blockChangeInstance);
                    }
                }
            }
            return new World(viewer, new WorldGenerator(seed, worldType, worldGenerationOptions), worldBlockChanges);
        } catch (Exception e) {
            throw new NBTParsingException("Loading of World failed", e);
        }
    }

}
