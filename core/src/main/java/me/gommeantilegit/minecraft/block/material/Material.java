package me.gommeantilegit.minecraft.block.material;

import me.gommeantilegit.minecraft.block.mapcolor.MapColor;
import org.jetbrains.annotations.NotNull;

public class Material {

    /**
     * Bool defining if the block can burn or not.
     */
    private boolean canBurn;

    /**
     * Determines whether blocks with this material can be "overwritten" by other blocks when placed - eg snow, vines
     * and tall grass.
     */
    private boolean replaceable;

    /**
     * Indicates if the material is translucent
     */
    private boolean isTranslucent;

    /**
     * The color index used to draw the blocks of this material on maps.
     */
    @NotNull
    private final MapColor materialMapColor;

    /**
     * Determines if the material can be harvested without a tool (or with the wrong tool)
     */
    private boolean requiresNoTool = true;

    /**
     * Mobility information flag. 0 indicates that this block is normal, 1 indicates that it can't push other blocks, 2
     * indicates that it can't be pushed.
     */
    private int mobilityFlag;

    /**
     * State whether blocks of this material type can be broken in adventure mode
     */
    private boolean isAdventureModeExempt;

    public Material(@NotNull MapColor color) {
        this.materialMapColor = color;
    }

    /**
     * Returns if blocks of these materials are liquids.
     */
    public boolean isLiquid() {
        return false;
    }

    /**
     * Returns true if the block is a considered solid. This is true by default.
     */
    public boolean isSolid() {
        return true;
    }

    /**
     * Will prevent grass from growing on dirt underneath and kill any grass below it if it returns true
     */
    public boolean blocksLight() {
        return true;
    }

    /**
     * Returns if this material is considered solid or not
     */
    public boolean blocksMovement() {
        return true;
    }

    /**
     * Marks the material as translucent
     */
    @NotNull
    Material setTranslucent() {
        this.isTranslucent = true;
        return this;
    }

    /**
     * Makes blocks with this material require the correct tool to be harvested.
     */
    @NotNull
    Material setRequiresTool() {
        this.requiresNoTool = false;
        return this;
    }

    /**
     * Set the canBurn bool to True and return the current object.
     */
    @NotNull
    Material setCanBurn() {
        this.canBurn = true;
        return this;
    }

    /**
     * Returns if the block can burn or not.
     */
    public boolean getCanBurn() {
        return this.canBurn;
    }

    /**
     * Sets {@link #replaceable} to true.
     */
    @NotNull
    Material setReplaceable() {
        this.replaceable = true;
        return this;
    }

    /**
     * Returns whether the material can be replaced by other blocks when placed - eg snow, vines and tall grass.
     */
    public boolean isReplaceable() {
        return this.replaceable;
    }

    /**
     * Indicate if the material is opaque
     */
    public boolean isOpaque() {
        return !this.isTranslucent && this.blocksMovement();
    }

    /**
     * Returns true if the material can be harvested without a tool (or with the wrong tool)
     */
    public boolean isToolNotRequired() {
        return this.requiresNoTool;
    }

    /**
     * Returns the mobility information of the material, 0 = free, 1 = can't push but can move over, 2 = total
     * immobility and stop pistons.
     */
    public int getMaterialMobility() {
        return this.mobilityFlag;
    }

    /**
     * This type of material can't be pushed, but pistons can move over it.
     */
    @NotNull
    protected Material setNoPushMobility() {
        this.mobilityFlag = 1;
        return this;
    }

    /**
     * This type of material can't be pushed, and pistons are blocked to move.
     */
    @NotNull
    protected Material setImmovableMobility() {
        this.mobilityFlag = 2;
        return this;
    }

    /**
     * @see #isAdventureModeExempt
     */
    @NotNull
    protected Material setAdventureModeExempt() {
        this.isAdventureModeExempt = true;
        return this;
    }

    /**
     * Retrieves the color index of the block. This is is the same color used by vanilla maps to represent this block.
     */
    @NotNull
    public MapColor getMaterialMapColor() {
        return this.materialMapColor;
    }

    public boolean isAdventureModeExempt() {
        return isAdventureModeExempt;
    }
}
