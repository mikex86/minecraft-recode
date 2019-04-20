package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.ServerBlockState;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerBlockChange extends WorldBlockChangerBase.ChangeBase<ServerBlockState, ServerChunk, ServerWorld> {

    ServerBlockChange(@NotNull BlockPos worldPosition, @Nullable ServerBlockState newBlockState, @NotNull ServerChunk chunk, @NotNull ServerWorld world) {
        super(worldPosition, newBlockState, chunk, world);
    }

    @Override
    void perform() {
        super.perform();
    }
}
