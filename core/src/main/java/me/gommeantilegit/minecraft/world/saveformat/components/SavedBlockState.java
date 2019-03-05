package me.gommeantilegit.minecraft.world.saveformat.components;

import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SavedBlockState {

    @NotNull
    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

    /**
     * Block Position that changed
     */
    @NotNull
    private final BlockPos position;

    /**
     * New Block at this position's id
     */
    private IBlockState blockState;

    public SavedBlockState(@NotNull BlockPos position, @Nullable IBlockState blockState) {
        this.position = position;
        this.blockState = blockState;
    }

    /**
     * Constructs a saved block state object from the specified parameters
     *
     * @param blockPos the block position which state should be captured
     * @param world    the world that holds the information of the block state for the specified position vector
     */
    public SavedBlockState(@NotNull BlockPos blockPos, @NotNull World world) {
        this(blockPos, world.getBlockState(blockPos));
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public SavedBlockState setNewBlock(@Nullable IBlockState newBlockState) {
        this.blockState = newBlockState;
        return this;
    }

    @NotNull
    public BlockPos getPosition() {
        return position;
    }

    public static class NBTConverter implements INBTConverter<NBTArray, SavedBlockState> {

        @NotNull
        @Override
        public NBTArray toNBTData(SavedBlockState object) {
            return new NBTArray(new NBTObject[]{
                    BlockState.NBT_CONVERTER.toNBTData(object.getBlockState()),
                    BlockPos.NBT_CONVERTER.toNBTData(object.getPosition())
            });
        }

        @NotNull
        @Override
        public SavedBlockState fromNBTData(NBTArray object, Object... args) throws NBTParsingException {
            return new SavedBlockState(
                    BlockPos.NBT_CONVERTER.fromNBTData((NBTArray) object.getValue()[1]),
                    BlockState.NBT_CONVERTER.fromNBTData((NBTArray) object.getValue()[0])
            );
        }
    }
}
