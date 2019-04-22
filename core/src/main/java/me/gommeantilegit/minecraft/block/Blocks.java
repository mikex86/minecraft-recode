package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.material.Material;
import me.gommeantilegit.minecraft.block.material.Materials;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;

public class Blocks {

    @NotNull
    public final HashMap<Integer, Block> blockRegistry = new HashMap<>();

    @NotNull
    protected final AbstractMinecraft mc;

    public Block stone;
    public Block dirt;
    public Block grass;
    public Block bedrock;

    public Blocks(@NotNull AbstractMinecraft mc) {
        this.mc = mc;
    }

    /**
     * Initializes blockStates and Builds texture map
     */
    public void init() {
        stone = new Block("stone", 1, Materials.rock).setSoundType("stone").setHardness(1.5f).setResistance(10f);
        dirt = new Block("dirt", 2, Materials.ground).setSoundType("gravel").setHardness(0.5f);
        grass = new Block("grass", 3, Materials.ground).setSoundType("grass").setHardness(0.6f);
        bedrock = new Block("bedrock", 8, Materials.rock).setHardness(-1).setSoundType("stone").setResistance(6000000.0F);
        registerAll(stone, dirt, grass, bedrock);
    }

    /**
     * Registers all the specified block instances in the block registry map
     *
     * @param blocks all blocks to register
     */
    public void registerAll(@NotNull Block... blocks) {
        for (Block block : blocks) {
            registerBlock(block);
        }
    }

    /**
     * Registers the block in the block registry map
     *
     * @param block the block to register
     */
    public void registerBlock(@NotNull Block block) {
        blockRegistry.put(block.getId(), block);
    }

    /**
     * @param id the id of a given block
     * @return the block instance with the specified id
     */
    @Nullable
    public Block getBlockByID(int id) {
        return blockRegistry.get(id);
    }

    @Nullable
    public IBlockState getDefaultBlockState(int blockID) {
        Block block = blockRegistry.get(blockID);
        if (block == null)
            return null;
        else return block.getDefaultBlockState();
    }

    /**
     * @return collection of all registered blocks
     */
    @NotNull
    public Collection<Block> getBlocks() {
        return blockRegistry.values();
    }
}
