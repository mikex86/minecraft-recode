package me.gommeantilegit.minecraft.entity.player.base;

import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.entity.living.LivingEntity;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinBase;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerBase<S extends SkinBase> extends LivingEntity {

    /**
     * Height of eyes above posY
     */
    public static final float EYE_HEIGHT = 1.6f;

    /**
     * State whether the player is sneaking
     * //TODO: IMPLEMENT
     */
    private boolean sneaking = false;

    /**
     * Player user name
     */
    @NotNull
    private final String userName;

    /**
     * The players skin
     */
    @NotNull
    private final S skin;

    /**
     * @param world     sets {@link #world}
     * @param maxHealth sets {@link #maxHealth}
     * @param username  sets {@link #userName}
     * @param skin      sets {@link #skin}
     */
    public PlayerBase(@Nullable WorldBase world, int maxHealth, @NotNull String username, @NotNull S skin) {
        super(world, maxHealth);
        this.userName = username;
        this.skin = skin;
    }

    @NotNull
    public String getUsername() {
        return userName;
    }

    /**
     * Checks if the player has the ability to harvest a block (checks current inventory item for a tool if necessary)
     */
    public boolean canHarvestBlock(BlockBase blockToHarvest) {
        //TODO: IMPLEMENT WHEN INVENTORY IS ADDED
        return true;
    }

    /**
     * BlockBase hardness will be further counted in {@link BlockBase#getPlayerRelativeBlockHardness}
     */
    public float getToolDigEfficiency(BlockBase blockToHarvest) {
        //TODO: IMPLEMENT WHEN INVENTORY IS ADDED
        return 1;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public PlayerBase setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
        return this;
    }

    @NotNull
    public S getSkin() {
        return skin;
    }
}
