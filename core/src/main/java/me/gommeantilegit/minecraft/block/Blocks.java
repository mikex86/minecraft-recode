package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.blocks.GrassBlock;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Blocks {

    private static final HashMap<Integer, Block> BLOCK_REGISTRY = new HashMap<>();

    public static Block STONE;
    public static Block DIRT;
    public static Block GRASS;
    public static Block QUARTZ;
    public static Block BEDROCK;

    public static void registerBlock(Block block) {
        BLOCK_REGISTRY.put(block.getId(), block);
    }

    @Nullable
    public static Block getBlockByID(int id) {
        return BLOCK_REGISTRY.get(id);
    }

    /**
     * Initializes blockStates and Builds texture map
     */
    public static void init() {
        /* Registering all blockStates */

        STONE = new Block("Stone", 1, new String[]{"stone"}, true).setHardness(1.5f).setResistance(10f);
        DIRT = new Block("Dirt", 2, new String[]{"dirt"}, true).setHardness(0.5f);
        GRASS = new GrassBlock().setHardness(0.6f);
        BEDROCK = new Block("Bedrock", 8, new String[]{"bedrock"}, true).setHardness(-1).setResistance(Long.MAX_VALUE);
        QUARTZ = new Block("Quartz", 155, new String[]{"quartz_block_top"}, true).setHardness(1.5f).setResistance(10f);

        Minecraft.mc.textureManager.blockTextureMap.build(); // Building the block texture map
    }

    @Nullable
    public static IBlockState getDefaultBlockState(int blockID) {
        Block block = BLOCK_REGISTRY.get(blockID);
        if(block == null)
            return null;
        else return block.getDefaultBlockState();
    }
}
