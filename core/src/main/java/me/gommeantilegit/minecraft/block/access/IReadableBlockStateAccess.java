package me.gommeantilegit.minecraft.block.access;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component that can provide a block state at a given position
 */
public interface IReadableBlockStateAccess {

    @Nullable
    IBlockState getBlockState(int x, int y, int z);

}
