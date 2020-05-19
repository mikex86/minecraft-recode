package me.gommeantilegit.minecraft.block.access;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Represents writable access to a block state storage of some sort
 */
public interface IWritableBlockStateAccess extends IReadableBlockStateAccess {

    void set(int x, int y, int z, @Nullable IBlockState blockState);

}
