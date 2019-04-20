package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.block.blocks.ClientGrassBlock;
import org.jetbrains.annotations.NotNull;

public class ClientBlocks extends Blocks<ClientBlock, ClientMinecraft> {

    public ClientBlocks(@NotNull ClientMinecraft clientMinecraft) {
        super(clientMinecraft);
    }

    @Override
    public void init() {
        stone = new ClientBlock("Stone", 1, new String[]{"stone"}, true, this, mc, false).setHardness(1.5f).setResistance(10f);
        dirt = new ClientBlock("Dirt", 2, new String[]{"dirt"}, true, this, mc, false).setHardness(0.5f);
        grass = new ClientGrassBlock(this, mc).setHardness(0.6f);
        bedrock = new ClientBlock("Bedrock", 8, new String[]{"bedrock"}, true, this, mc, false).setHardness(-1).setResistance(Long.MAX_VALUE);
        quartz = new ClientBlock("Quartz", 155, new String[]{"quartz_block_bottom"}, true, this, mc, false).setHardness(1.5f).setResistance(10f);
    }

    public void buildTextureMap(){
        mc.textureManager.blockTextureMap.build(); // Building the block texture map
    }

}
