package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.block.state.ServerBlockState;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;

public class ServerBlock extends BlockBase<ServerMinecraft, ServerBlock, ServerBlockState, ServerBlocks> {

    public ServerBlock(@NotNull String name, int id, boolean collidable, @NotNull ServerBlocks blocks, ServerMinecraft mc, boolean hasEnumFacing) {
        super(name, id, hasEnumFacing, collidable, blocks, mc);
    }

    @NotNull
    @Override
    public ServerBlockState getDefaultBlockState() {
        return new ServerBlockState(this, EnumFacing.UP);
    }

}
