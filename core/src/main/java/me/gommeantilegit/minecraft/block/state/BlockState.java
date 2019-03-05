package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.nbt.NBTObject;
import me.gommeantilegit.minecraft.nbt.api.INBTConverter;
import me.gommeantilegit.minecraft.nbt.exception.NBTParsingException;
import me.gommeantilegit.minecraft.nbt.impl.NBTArray;
import me.gommeantilegit.minecraft.nbt.impl.NBTInteger;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of IBlockState
 */
public class BlockState implements IBlockState {

    public static final NBTConverter NBT_CONVERTER = new NBTConverter();

    @NotNull
    private Block block;

    @NotNull
    private EnumFacing facing;

    public BlockState(@NotNull Block block, @NotNull EnumFacing facing) {
        this.block = block;
        this.facing = facing;
    }

    @Override
    @NotNull
    public Block getBlock() {
        return block;
    }

    @NotNull
    @Override
    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public void setFacing(@NotNull EnumFacing facing) {
        this.facing = facing;
    }

    @Override
    public void setBlock(@Nullable Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "BlockState{blockName: " + block.getName() + ", id: " + block.getId() + ", Facing: " + facing.name() + "}";
    }

    public static final class NBTConverter implements INBTConverter<NBTArray, IBlockState> {

        @NotNull
        @Override
        public NBTArray toNBTData(IBlockState object) {
            return new NBTArray(new NBTObject[]{
                    new NBTInteger(object == null ? 0 : object.getBlock().getId()),
                    new NBTInteger(object == null ? 0 : object.getFacing().ordinal())
            });
        }

        @Nullable
        @Override
        public IBlockState fromNBTData(NBTArray object, Object... args) throws NBTParsingException {
            try {
                int blockID = ((NBTInteger) object.getValue()[0]).getValue();
                int facingOrdinal = ((NBTInteger) object.getValue()[1]).getValue();
                Block block = Blocks.getBlockByID(blockID);
                EnumFacing facing = EnumFacing.values()[facingOrdinal];
                if (block == null)
                    return null;
                return new BlockState(block, facing);
            } catch (Exception e) {
                throw new NBTParsingException("Parsing IBlockState from NBTData failed!", e);
            }
        }

    }
}
