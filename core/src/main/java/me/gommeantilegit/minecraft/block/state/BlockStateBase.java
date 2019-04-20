package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of IBlockState
 */
public class BlockStateBase implements IBlockState {

    /**
     * The block instance
     */
    private BlockBase block;

    /**
     * Facing direction of the block
     */
    @NotNull
    private EnumFacing facing;

    public BlockStateBase(@NotNull BlockBase block, @NotNull EnumFacing facing) {
        this.block = block;
        this.facing = facing;
    }

    @Override
    @NotNull
    public BlockBase getBlock() {
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
    public void setBlock(@NotNull BlockBase block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "BlockState{blockName: " + block.getName() + ", id: " + block.getId() + ", Facing: " + facing.name() + "}";
    }

    @Override
    public BlockStateBase clone() {
        return new BlockStateBase(block, facing);
    }
}
