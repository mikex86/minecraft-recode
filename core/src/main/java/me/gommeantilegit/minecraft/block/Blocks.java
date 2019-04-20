package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Blocks {

    @NotNull
    public final HashMap<Integer, BlockBase> blockRegistry = new HashMap<>();

    @NotNull
    protected final AbstractMinecraft mc;

    public BlockBase stone;
    public BlockBase dirt;
    public BlockBase grass;
    public BlockBase quartz;
    public BlockBase bedrock;

    public Blocks(@NotNull AbstractMinecraft mc) {
        this.mc = mc;
    }

    /**
     * Initializes blockStates and Builds texture map
     */
    public void init() {
        stone = new BlockBase("Stone", 1, true, true).setHardness(1.5f).setResistance(10f);
        dirt = new BlockBase("Dirt", 2, true, true).setHardness(0.5f);
        grass = new BlockBase("Grass", 3, true, true).setHardness(0.6f);
        bedrock = new BlockBase("Bedrock", 8, true, true).setHardness(-1).setResistance(Long.MAX_VALUE);
        quartz = new BlockBase("Quartz", 155, true, true).setHardness(1.5f).setResistance(10f);
        registerAll(stone, dirt, grass, bedrock, quartz);
    }

    /**
     * Registers all the specified block instances in the block registry map
     * @param blocks all blocks to register
     */
    public void registerAll(BlockBase... blocks){
        for (BlockBase block : blocks) {
            registerBlock(block);
        }
    }

    /**
     * Registers the block in the block registry map
     * @param block the block to register
     */
    public void registerBlock(BlockBase block) {
        blockRegistry.put(block.getId(), block);
    }

    /**
     * @param id the id of a given block
     * @return the block instance with the specified id
     */
    @Nullable
    public BlockBase getBlockByID(int id) {
        return blockRegistry.get(id);
    }

    @Nullable
    public IBlockState getDefaultBlockState(int blockID) {
        BlockBase block = blockRegistry.get(blockID);
        if (block == null)
            return null;
        else return block.getDefaultBlockState();
    }
}
