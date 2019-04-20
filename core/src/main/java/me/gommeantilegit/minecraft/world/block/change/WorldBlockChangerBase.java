package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.BlockStateBase;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Object handling the calls to changing blockStates of the world
 */
public abstract class WorldBlockChangerBase<WB extends WorldBase, CHB extends WorldBlockChangerBase.ChangeBase, BS extends BlockStateBase, CB extends ChunkBase> implements AsyncOperation {

    /**
     * Parent world instance
     */
    @NotNull
    protected final WB world;

    /**
     * BlockChanges to perform by the object
     */
    @NotNull
    protected final Queue<CHB> toPerform = new LinkedList<>();

    /**
     * State whether block changes are scheduled to be performed or not
     */
    protected boolean changesToPerform = false;

    /**
     * @param world sets {@link #world}
     */
    public WorldBlockChangerBase(@NotNull WB world) {
        this.world = world;
    }

    /**
     * Represents a block change to be performed by the Changer
     */
    protected static class ChangeBase<BS extends BlockStateBase, CB extends ChunkBase, WB extends WorldBase> {

        /**
         * Position where the change should be performed
         */
        @NotNull
        protected final BlockPos worldPosition;

        /**
         * The id of the new block at this position
         */
        @Nullable
        private final BS newBlockState;

        /**
         * The chunk handling the block position {@link #worldPosition}
         */
        @NotNull
        private final CB chunk;

        /**
         * World instance
         */
        @NotNull
        protected final WB world;

        ChangeBase(@NotNull BlockPos worldPosition, @Nullable BS newBlockState, @NotNull CB chunk, @NotNull WB world) {
            this.worldPosition = worldPosition;
            this.newBlockState = newBlockState;
            this.chunk = chunk;
            this.world = world;
        }

        /**
         * Performs the block change
         */
        void perform() {
            chunk.setBlockWithoutWorldBlockChangerObject(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), newBlockState);
        }
    }

    @Override
    public void onAsyncThread() {
        if(changesToPerform) {
            synchronized (toPerform) {
                while (!this.toPerform.isEmpty()) {
                    try {
                        CHB change = this.toPerform.remove();
                        change.perform();
                    } catch (NoSuchElementException ignored) {
                    }
                }
                changesToPerform = false;
            }
        }
    }

    /**
     * @param x             x coordinate
     * @param y             y coordinate
     * @param z             z coordinate
     * @param newBlockState the id of the new block
     */
    public abstract void blockChange(int x, int y, int z, @Nullable BS newBlockState, @NotNull CB chunk);

}
