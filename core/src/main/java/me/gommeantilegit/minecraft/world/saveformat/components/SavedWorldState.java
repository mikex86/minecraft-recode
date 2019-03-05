package me.gommeantilegit.minecraft.world.saveformat.components;

import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTInteger;
import me.gommeantilegit.minecraft.nbt.impl.NBTLong;
import me.gommeantilegit.minecraft.nbt.impl.NBTStringMap;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A world state as an object
 */
public class SavedWorldState {

    @NotNull
    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

    /**
     * Collection of saved chunk state
     */
    @NotNull
    private final Collection<SavedChunkState> chunkStates;

    /**
     * Seed of the world
     */
    private final long worldSeed;

    /**
     * Type of the world
     */
    @NotNull
    private final WorldGenerator.WorldType worldType;

    /**
     * World generation options
     */
    @NotNull
    private final WorldGenerationOptions worldGenerationOptions;

    /**
     * Height of the world
     */
    private final int worldHeight;

    public SavedWorldState(@NotNull Collection<SavedChunkState> chunkStates, long worldSeed, @NotNull WorldGenerator.WorldType worldType, @NotNull WorldGenerationOptions worldGenerationOptions, int worldHeight) {
        this.chunkStates = chunkStates;
        this.worldSeed = worldSeed;
        this.worldType = worldType;
        this.worldGenerationOptions = worldGenerationOptions;
        this.worldHeight = worldHeight;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    /**
     * Constructs a world state object from the current state of the specified world
     *
     * @param world the specified world
     */
    public SavedWorldState(@NotNull World world) {
        this(computeChunkStates(world), world.getWorldGenerator().getSeed(), world.getWorldGenerator().getWorldType(), world.getWorldGenerator().getWorldGenerationOptions(), world.getHeight());
    }

    /**
     * Computes the saved chunk state objects for the specified world
     *
     * @param world the specified world
     * @return the chunk states
     */
    @NotNull
    private static Collection<SavedChunkState> computeChunkStates(@NotNull World world) {
        List<SavedChunkState> states = new ArrayList<>();
        for (Chunk chunk : world.getWorldChunkHandler().getChunks())
            states.add(new SavedChunkState(chunk));
        return states;
    }

    @NotNull
    public Collection<SavedChunkState> getChunkStates() {
        return chunkStates;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    @NotNull
    public WorldGenerationOptions getWorldGenerationOptions() {
        return worldGenerationOptions;
    }

    @NotNull
    public WorldGenerator.WorldType getWorldType() {
        return worldType;
    }


    public static class NBTConverter implements INBTConverter<NBTArray, SavedWorldState> {

        @NotNull
        @Override
        public NBTArray toNBTData(@NotNull SavedWorldState object) {
            return new NBTArray(new NBTObject[]
                    {
                            new NBTLong(object.getWorldSeed()),
                            new NBTInteger(object.getWorldType().ordinal()),
                            new NBTInteger(object.getWorldHeight()),
                            WorldGenerationOptions.NBT_CONVERTER.toNBTData(object.getWorldGenerationOptions()),
                            chunkStatesToNBT(object.getChunkStates())
                    });
        }

        /**
         * @param chunkStates all chunks states saved
         * @return the nbt representation
         */
        @NotNull
        private NBTArray chunkStatesToNBT(@NotNull Collection<SavedChunkState> chunkStates) {
            NBTObject[] objects = new NBTObject[chunkStates.size()];
            int i = 0;
            for (SavedChunkState savedChunkState : chunkStates)
                objects[i++] = SavedChunkState.NBT_CONVERTER.toNBTData(savedChunkState);
            return new NBTArray(objects);
        }

        @NotNull
        @Override
        public SavedWorldState fromNBTData(@NotNull NBTArray object, Object... args) throws NBTParsingException {
            NBTObject<?>[] array = object.getValue();
            long seed = (Long) array[0].getValue();
            WorldGenerator.WorldType worldType = WorldGenerator.WorldType.values()[(Integer) array[1].getValue()];
            int worldHeight = (Integer) array[2].getValue();
            WorldGenerationOptions worldGenerationOptions = WorldGenerationOptions.NBT_CONVERTER.fromNBTData((NBTStringMap) array[3]);
            NBTObject<?>[] chunkStatesArray = ((NBTArray) array[4]).getValue();
            List<SavedChunkState> chunkStates = new ArrayList<>(50);
            for (NBTObject<?> chunkState : chunkStatesArray) {
                SavedChunkState savedChunkState = SavedChunkState.NBT_CONVERTER.fromNBTData((NBTArray) chunkState, worldHeight);
                chunkStates.add(savedChunkState);
            }
            return new SavedWorldState(chunkStates, seed, worldType, worldGenerationOptions, worldHeight);
        }
    }
}
