package me.gommeantilegit.minecraft.world.saveformat.components;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTInteger;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.util.math.vecmath.nbt.Vector2NBTConverter;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import static me.gommeantilegit.minecraft.world.chunk.Chunk.CHUNK_SIZE;

/**
 * The state of the chunk
 */
public class SavedChunkState {

    @NotNull
    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

    /**
     * Block states of the chunk
     */
    @NotNull
    private final SavedBlockState[][][] blockStates;

    /**
     * (x, z) chunk origin position
     *
     * @see Chunk#getChunkOrigin()
     */
    @NotNull
    private final Vector2 chunkOrigin;

    /**
     * @param blockStates sets {@link #blockStates}
     * @param chunkOrigin sets {@link #chunkOrigin}
     */
    public SavedChunkState(@NotNull SavedBlockState[][][] blockStates, @NotNull Vector2 chunkOrigin) {
        this.blockStates = blockStates;
        this.chunkOrigin = chunkOrigin;
    }

    /**
     * Constructs a chunk state from the given chunk
     *
     * @param chunk the given chunk
     */
    public SavedChunkState(@NotNull Chunk chunk) {
        this(computeBlockStates(chunk), chunk.getChunkOrigin());
    }

    @NotNull
    private static SavedBlockState[][][] computeBlockStates(@NotNull Chunk chunk) {
        SavedBlockState[][][] states = new SavedBlockState[Chunk.CHUNK_SIZE][chunk.getWorld().height][Chunk.CHUNK_SIZE];
        for (int xo = 0; xo < Chunk.CHUNK_SIZE; xo++) {
            for (int yo = 0; yo < chunk.getWorld().height; yo++) {
                for (int zo = 0; zo < Chunk.CHUNK_SIZE; zo++) {
                    BlockPos blockPos = new BlockPos(chunk.getX() + xo, yo, chunk.getZ() + zo);
                    states[xo][yo][zo] = new SavedBlockState(blockPos, chunk.getWorld());
                }
            }
        }
        return states;
    }

    @NotNull
    public SavedBlockState[][][] getBlockStates() {
        return blockStates;
    }

    @NotNull
    public Vector2 getChunkOrigin() {
        return chunkOrigin;
    }

    public static class NBTConverter implements INBTConverter<NBTArray, SavedChunkState> {

        @NotNull
        @Override
        public NBTArray toNBTData(SavedChunkState object) {
            NBTObject[] objects = new NBTObject[3];
            int worldHeight = object.getBlockStates()[0].length;
            objects[0] = Vector2NBTConverter.INSTANCE.toNBTData(object.getChunkOrigin());
            NBTArray[] blockStates = new NBTArray[CHUNK_SIZE * worldHeight * CHUNK_SIZE];
            int index = 0;
            for (int xo = 0; xo < CHUNK_SIZE; xo++)
                for (int yo = 0; yo < worldHeight; yo++)
                    for (int zo = 0; zo < CHUNK_SIZE; zo++)
                        if (object.getBlockStates()[xo][yo][zo].getBlockState() != null)
                            blockStates[index++] = SavedBlockState.NBT_CONVERTER.toNBTData(object.getBlockStates()[xo][yo][zo]);
                        else index++;
            assert blockStates.length == CHUNK_SIZE * CHUNK_SIZE * worldHeight;
            assert blockStates.length == index;
            objects[1] = new NBTInteger(blockStates.length);
            objects[2] = new NBTArray(blockStates);
            return new NBTArray(objects);
        }


        /**
         * @param object the NBTObject to be parsed into an instance of type C
         * @param args   extra arguments if needed
         * @return the saved chunk state parsed from the specified nbt object
         * @throws NBTParsingException if parsing fails
         * @apiNote args[0] must be set to the words height
         */
        @NotNull
        @Override
        public SavedChunkState fromNBTData(NBTArray object, Object... args) throws NBTParsingException {
            assert args.length == 1;
            assert args[0] instanceof Integer;

            int worldHeight = (int) args[0];
            NBTObject[] objects = object.getValue();
            Vector2 origin = Vector2NBTConverter.INSTANCE.fromNBTData((NBTArray) objects[0]);
            int length = (Integer) objects[1].getValue();
            NBTArray blockStatesArray = (NBTArray) objects[2];
            NBTObject[] savedBlockStates = blockStatesArray.getValue();
            SavedBlockState[][][] savedBlockStatesOut = new SavedBlockState[CHUNK_SIZE][worldHeight][CHUNK_SIZE];
            int index = 0;
            for (int xo = 0; xo < CHUNK_SIZE; xo++)
                for (int yo = 0; yo < worldHeight; yo++)
                    for (int zo = 0; zo < CHUNK_SIZE; zo++)
                        if (savedBlockStates[index] != null)
                            savedBlockStatesOut[xo][yo][zo] = SavedBlockState.NBT_CONVERTER.fromNBTData((NBTArray) savedBlockStates[index++]);
                        else index++;
            assert index == length;
            assert index == CHUNK_SIZE * worldHeight * CHUNK_SIZE;
            return new SavedChunkState(savedBlockStatesOut, origin);
        }
    }
}
