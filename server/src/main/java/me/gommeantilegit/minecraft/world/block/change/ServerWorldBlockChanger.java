package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.ServerBlockState;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.ServerWorld;
import me.gommeantilegit.minecraft.world.chunk.ServerChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerWorldBlockChanger extends WorldBlockChangerBase<ServerWorld, ServerBlockChange, ServerBlockState, ServerChunk> {

    /**
     * @param world sets {@link #world}
     */
    public ServerWorldBlockChanger(@NotNull ServerWorld world) {
        super(world);
    }

    @Override
    public void blockChange(int x, int y, int z, @Nullable ServerBlockState newBlockState, @NotNull ServerChunk chunk) {
        if (world.mc.isCallingFromMinecraftThread())
            chunk.setBlockWithoutWorldBlockChangerObject(x, y, z, newBlockState);
        else {
            this.toPerform.add(new ServerBlockChange(new BlockPos(x, y, z), newBlockState, chunk, world));
            this.changesToPerform = true;
        }
    }

}
