package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockChange {

    /**
     * Block Position that changed
     */
    @NotNull
    private final BlockPos position;

    /**
     * New Block at this position's id
     */
    private IBlockState newBlockState;

    /**
     * Original block at this state from world creator
     */
    private final IBlockState originalBlockState;

    public BlockChange(@NotNull BlockPos position, @Nullable IBlockState newBlockState, @Nullable IBlockState originalBlock) {
        this.position = position;
        this.newBlockState = newBlockState;
        this.originalBlockState = originalBlock;
    }

    public IBlockState getNewBlockState() {
        return newBlockState;
    }

    public BlockChange setNewBlock(@Nullable IBlockState newBlockState) {
        this.newBlockState = newBlockState;
        return this;
    }

    public IBlockState getOriginalBlockState() {
        return originalBlockState;
    }

    @NotNull
    public BlockPos getPosition() {
        return position;
    }
}
