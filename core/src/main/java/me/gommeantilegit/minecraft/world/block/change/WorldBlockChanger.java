package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Object handling the calls to changing blockStates of the world
 */
public class WorldBlockChanger implements AsyncOperation {

    /**
     * Parent world instance
     */
    @NotNull
    private final World world;

    /**
     * BlockChanges to perform by the object
     */
    @NotNull
    private final Queue<Change> toPerform = new LinkedList<>();

    /**
     * @param world sets {@link #world}
     */
    public WorldBlockChanger(@NotNull World world) {
        this.world = world;
    }

    /**
     * Represents a block change to be performed by the Changer
     */
    private final class Change {

        /**
         * Position where the change should be performed
         */
        @NotNull
        private final BlockPos worldPosition;

        /**
         * The id of the new block at this position
         */
        @Nullable
        private final IBlockState newBlockState;

        /**
         * The chunk handling the block position {@link #worldPosition}
         */
        @NotNull
        private final Chunk chunk;

        /**
         * State whether or not the change should be added as a blockChange to {@link me.gommeantilegit.minecraft.world.World#blockChanges}
         */
        private final boolean addBlockChange;

        Change(@NotNull BlockPos worldPosition, @Nullable IBlockState newBlockState, @NotNull Chunk chunk, boolean addBlockChange) {
            this.worldPosition = worldPosition;
            this.newBlockState = newBlockState;
            this.chunk = chunk;
            this.addBlockChange = addBlockChange;
        }

        /**
         * Performs the block change
         */
        void perform() {
            if (addBlockChange)
                chunk.setBlockWithoutWorldBlockChangerObject(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), newBlockState);
            else
                chunk.setBlockNoChangeWithoutWorldBlockChangerObject(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), newBlockState);
            world.rebuildChunksFor(worldPosition.getX(), worldPosition.getZ());
        }
    }

    @Override
    public void onAsyncThread() {
        while (!this.toPerform.isEmpty()) {
            try {
                Change change = this.toPerform.remove();
                change.perform();
            } catch (NoSuchElementException ignored) {
            }
        }
    }

    /**
     * @param x              x coordinate
     * @param y              y coordinate
     * @param z              z coordinate
     * @param newBlockState     the id of the new block
     * @param addBlockChange State whether or not the change should be added as a blockChange to {@link me.gommeantilegit.minecraft.world.World#blockChanges}
     */
    public void blockChange(int x, int y, int z, @Nullable IBlockState newBlockState, boolean addBlockChange, @NotNull Chunk chunk) {
        this.toPerform.add(new Change(new BlockPos(x, y, z), newBlockState, chunk, addBlockChange));
    }

}
