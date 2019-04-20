package me.gommeantilegit.minecraft.world.block.change;

import me.gommeantilegit.minecraft.block.state.ClientBlockState;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientBlockChanger extends WorldBlockChangerBase<ClientWorld, ClientBlockChange, ClientBlockState, ClientChunk> {

    /**
     * @param world sets {@link #world}
     */
    public ClientBlockChanger(@NotNull ClientWorld world) {
        super(world);
    }

    @Override
    public void blockChange(int x, int y, int z, @Nullable ClientBlockState newBlockState, @NotNull ClientChunk chunk) {
        if (world.mc.isCallingFromMinecraftThread())
            chunk.setBlockWithoutWorldBlockChangerObject(x, y, z, newBlockState);
        else {
            synchronized (toPerform) {
                this.toPerform.add(new ClientBlockChange(new BlockPos(x, y, z), newBlockState, chunk, world, true));
                this.changesToPerform = true;
            }
        }
    }
}
