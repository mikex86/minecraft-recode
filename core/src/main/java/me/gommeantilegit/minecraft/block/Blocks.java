package me.gommeantilegit.minecraft.block;

import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.annotations.ThreadSafe;
import me.gommeantilegit.minecraft.block.material.Materials;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.palette.GlobalBlockStatePalette;
import me.gommeantilegit.minecraft.block.state.palette.PaletteVersionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Blocks {

    @NotNull
    protected final AbstractMinecraft mc;

    // the block instances

    public Block stone;
    public Block dirt;
    public Block grass;
    public Block bedrock;

    /**
     * All possible block states
     */
    @NotNull
    private List<IBlockState> possibleBlockStates = new ArrayList<>();

    /**
     * Stores all registered block instances
     */
    @NotNull
    private List<Block> registeredBlocks = new ArrayList<>();

    /**
     * Manages access to palette version specific block state lists
     */
    @Nullable
    private PaletteVersionManager paletteVersionManager;

    /**
     * The global block state palette
     */
    @Nullable
    private GlobalBlockStatePalette globalPalette;

    public Blocks(@NotNull AbstractMinecraft mc) {
        this.mc = mc;
    }

    // TODO: RE-ADD IDS FOR SORTING OF REGISTERED BLOCKS SO THAT THE GLOBAL BLOCK-STATE PALETTE ISN'T RANDOMLY ORDERED DEPENDING ON THE ORDER OF REGISTRATION

    /**
     * Initializes blockStates and Builds texture map
     */
    public void init() {
        stone = new Block(1, 1, "stone", Materials.rock).setSoundType("stone").setHardness(1.5f).setResistance(10f);
        dirt = new Block(1, 3, "dirt", Materials.ground).setSoundType("gravel").setHardness(0.5f);
        grass = new Block(1, 2, "grass", Materials.ground).setSoundType("grass").setHardness(0.6f);
        bedrock = new Block(1, 7, "bedrock", Materials.rock).setHardness(-1).setSoundType("stone").setResistance(6000000.0F);
        registerAll(stone, dirt, grass, bedrock);
        finalizeRegistration();
        initBlockStates();
    }

    /**
     * Finalizes the block registration process
     */
    private void finalizeRegistration() {
        this.registeredBlocks.sort(Comparator.comparingInt(Block::getId)); // sort ascending by id
        this.globalPalette = new GlobalBlockStatePalette(this);
        this.paletteVersionManager = new PaletteVersionManager(this);
    }

    /**
     * Initializes {@link #possibleBlockStates}.
     * Block registration must be finalized before this is called
     *
     * @see #finalizeRegistration()
     */
    private void initBlockStates() {
        for (Block block : this.registeredBlocks) {
            this.possibleBlockStates.addAll(block.getPossibleBlockStates());
        }
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
        registeredBlocks.add(block);
    }

    /**
     * @return collection of all registered blocks
     */
    @NotNull
    public Collection<Block> getBlocks() {
        return registeredBlocks;
    }

    @NotNull
    public List<IBlockState> getPossibleBlockStates() {
        return this.possibleBlockStates;
    }

    @NotNull
    @ThreadSafe
    public List<IBlockState> getPossibleBlockStates(int paletteVersion) {
        return Objects.requireNonNull(paletteVersionManager, "Blocks not yet initialized!").get(paletteVersion);
    }

    @NotNull
    public GlobalBlockStatePalette getGlobalPalette() {
        return Objects.requireNonNull(globalPalette, "Blocks not yet initialized!");
    }
}
