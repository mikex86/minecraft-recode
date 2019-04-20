package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import org.jetbrains.annotations.NotNull;

@SideOnly(side = Side.SERVER)
public class ServerBlocks extends Blocks<ServerBlock, ServerMinecraft> {

    public ServerBlocks(@NotNull ServerMinecraft mc) {
        super(mc);
    }

    @Override
    public void init() {
        stone = new ServerBlock("Stone", 1, true, this, mc, false).setHardness(1.5f).setResistance(10f);
        dirt = new ServerBlock("Dirt", 2, true, this, mc, false).setHardness(0.5f);
        grass = new ServerBlock("Grass", 3, true, this, mc, false).setHardness(0.6f);
        bedrock = new ServerBlock("Bedrock", 8, true, this, mc, false).setHardness(-1).setResistance(Long.MAX_VALUE);
        quartz = new ServerBlock("Quartz", 155, true, this, mc, false).setHardness(1.5f).setResistance(10f);
    }

}
