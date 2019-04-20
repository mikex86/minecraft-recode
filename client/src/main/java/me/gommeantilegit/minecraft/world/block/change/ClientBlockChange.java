package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.ClientBlockState;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientBlockChange extends WorldBlockChangerBase.ChangeBase<ClientBlockState, ClientChunk, ClientWorld> {

    /**
     * State whether the chunk of the block position of the block change should be the first of the rebuild queue to be rebuilt
     */
    private final boolean highPriority;

    ClientBlockChange(@NotNull BlockPos worldPosition, @Nullable ClientBlockState newBlockState, @NotNull ClientChunk chunk, @NotNull ClientWorld world, boolean highPriority) {
        super(worldPosition, newBlockState, chunk, world);
        this.highPriority = highPriority;
    }

    @Override
    void perform() {
        super.perform();
        world.rebuildChunksFor(worldPosition.getX(), worldPosition.getZ(), highPriority);
    }
}
