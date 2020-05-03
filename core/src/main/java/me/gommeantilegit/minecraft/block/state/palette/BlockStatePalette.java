package me.gommeantilegit.minecraft.block.state.palette;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.utils.palette.IPalette;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link IPalette} implementation for all possible block-states
 */
public class BlockStatePalette implements IPalette<IBlockState> {

    @NotNull
    private final List<IBlockState> possibleBlockStates;

    public BlockStatePalette(@NotNull Blocks mcBlocks) {
        this.possibleBlockStates = new ArrayList<>();
        Collection<Block> blockList = mcBlocks.getBlocks();
        for (Block block : blockList) {
            this.possibleBlockStates.addAll(block.getPossibleBlockStates());
        }
    }

    @Nullable
    @Override
    public IBlockState getInstance(long index) {
        if (index == 0)
            return null;
        index--;
        try {
            return this.possibleBlockStates.get((int) index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public long getNumKeys() {
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
