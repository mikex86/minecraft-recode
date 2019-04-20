package me.gommeantilegit.minecraft.block.state;

import me.gommeantilegit.minecraft.MinecraftProvider;
import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Implementation of IBlockState
 *
 * @param <T> type of BlockBase
 */
public abstract class BlockStateBase<T extends BlockBase> implements IBlockState<T>, Serializable {

    /**
     * The block instance
     */
    private transient T block;

    /**
     * The id of the block.
     * Used to restore the value of {@link #block} after deserialization
     */
    private final int blockID;

    /**
     * Id of the minecraft instance used in the specified block instance ({@link #block})
     */
    private final long minecraftID;

    /**
     * Facing direction of the block
     */
    @NotNull
    private EnumFacing facing;

    public BlockStateBase(@NotNull T block, @NotNull EnumFacing facing) {
        this.block = block;
        this.facing = facing;
        this.blockID = block.getId();
        this.minecraftID = block.mc.id;
    }

    @Override
    @NotNull
    public T getBlock() {
        checkInstance();
        return block;
    }

    @NotNull
    @Override
    public EnumFacing getFacing() {
        checkInstance();
        return facing;
    }

    /**
     * Checks if {@link #block} is null meaning this instance was created by deserialization and if it is, the value of {@link #block} is restored
     * accordingly using {@link #blockID}.
     */
    private void checkInstance() {
        if (block == null)
            block = (T) MinecraftProvider.getMC(minecraftID).blocks.getBlockByID(blockID);
    }

    @Override
    public void setFacing(@NotNull EnumFacing facing) {
        checkInstance();
        this.facing = facing;
    }

    @Override
    public void setBlock(@NotNull T block) {
        this.block = block;
    }

    @Override
    public String toString() {
        checkInstance();
        return "BlockStateBase{blockName: " + block.getName() + ", id: " + block.getId() + ", Facing: " + facing.name() + "}";
    }
    @Override
    public abstract BlockStateBase<T> clone();
}
