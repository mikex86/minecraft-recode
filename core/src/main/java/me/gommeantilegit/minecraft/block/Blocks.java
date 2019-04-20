package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class Blocks<T extends BlockBase, MC extends AbstractMinecraft> {

    @NotNull
    private final HashMap<Integer, T> blockRegistry = new HashMap<>();

    @NotNull
    protected final MC mc;

    public T stone;
    public T dirt;
    public T grass;
    public T quartz;
    public T bedrock;

    protected Blocks(@NotNull MC mc) {
        this.mc = mc;
    }

    public void registerBlock(T block) {
        blockRegistry.put(block.getId(), block);
    }

    @Nullable
    public T getBlockByID(int id) {
        return blockRegistry.get(id);
    }

    /**
     * Initializes blockStates and Builds texture map
     */
    public abstract void init();

    @Nullable
    public IBlockState getDefaultBlockState(int blockID) {
        BlockBase block = blockRegistry.get(blockID);
        if(block == null)
            return null;
        else return block.getDefaultBlockState();
    }
}
