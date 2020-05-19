package me.gommeantilegit.minecraft.block.state.palette;

import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.utils.palette.IPalette;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A {@link IPalette} implementation for all possible block-states
 */
public class GlobalBlockStatePalette implements IBlockStatePalette {

    @NotNull
    private final List<IBlockState> possibleBlockStates;

    public GlobalBlockStatePalette(@NotNull Blocks blocks) {
        this.possibleBlockStates = blocks.getPossibleBlockStates();
    }

    @Nullable
    @Override
    public IBlockState getInstance(int index) {
        if (index == 0)
            return null;
        index--;
        int access = index;
        if (access < this.possibleBlockStates.size())
            return this.possibleBlockStates.get(access);
        else
            return null;
    }

    @Override
    public int getNumKeys() {
        return this.possibleBlockStates.size() + 1;
    }

    @Override
    public int getInstanceIndex(@Nullable IBlockState value) {
        int index = this.possibleBlockStates.indexOf(value);
        if (index == -1) {
            return 0;
        }
        return index + 1;
    }
}
