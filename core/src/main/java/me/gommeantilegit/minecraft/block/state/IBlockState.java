package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IBlockState {

    /**
     * @return the block of the state
     */
    @Nullable
    Block getBlock();

    /**
     * @return the block facing of the state
     */
    @NotNull
    EnumFacing getFacing();

    /**
     * Setting the facing of the state
     */
    void setFacing(@NotNull EnumFacing facing);

    /*+
     * Setting the block of the state
     */
    void setBlock(@NotNull Block block);

    @Override
    String toString();

}
