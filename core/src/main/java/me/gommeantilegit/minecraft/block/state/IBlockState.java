package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IBlockState{

    /**
     * @return the block of the state
     */
    @NotNull
    BlockBase getBlock();

    /**
     * @return the block facing of the state
     */
    @NotNull
    EnumFacing getFacing();

    /**
     * Sets the facing of the state
     */
    void setFacing(@NotNull EnumFacing facing);

    /**
     * Sets the block of the state
     */
    void setBlock(@NotNull BlockBase block);

    @Override
    String toString();

}
