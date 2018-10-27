package me.michael.kei.minecraft.block;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Blocks {

    private static final HashMap<Integer, Block> BLOCK_REGISTRY = new HashMap<>();

    public static final Block STONE = new Block("Stone", 1, new String[]{"stone"});
    public static final Block DIRT = new Block("Dirt", 2, new String[]{"dirt"});

    static {
        Block.TEXTURE_MAP.build();
    }

    public static void registerBlock(Block block) {
        BLOCK_REGISTRY.put(block.getId(), block);
    }

    @Nullable
    public static Block getBlockByID(int id) {
        return BLOCK_REGISTRY.get(id);
    }
}
