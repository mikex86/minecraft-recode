package me.gommeantilegit.minecraft.entity.player.base;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.entity.living.LivingEntity;
import me.gommeantilegit.minecraft.entity.player.base.skin.SkinBase;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerBase extends LivingEntity {

    /**
     * Height of eyes above posY
     */
    public static final float DEFAULT_EYE_HEIGHT = 1.62F;

    /**
     * State whether the player is sneaking
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
    private final SkinBase skin;

    /**
     * @param world     sets {@link #world}
     * @param maxHealth sets {@link #maxHealth}
     * @param username  sets {@link #userName}
     * @param skin      sets {@link #skin}
     */
    public PlayerBase(@Nullable WorldBase world, int maxHealth, @NotNull String username, @NotNull SkinBase skin) {
        super(world, maxHealth);
        this.userName = username;
        this.skin = skin;
    }

    public float getEyeHeight() {
        float f = DEFAULT_EYE_HEIGHT;

        // TODO: WHEN SLEEPING IS IMPLEMENTED
//        if (this.isPlayerSleeping())
//        {
//            f = 0.2F;
//        }

        if (this.isSneaking()) {
            f -= 0.08F;
        }
        return f;
    }

    @NotNull
    public String getUsername() {
        return userName;
    }

    /**
     * Checks if the player has the ability to harvest a block (checks current inventory item for a tool if necessary)
     */
    public boolean canHarvestBlock(@NotNull Block blockToHarvest) {
        //TODO: IMPLEMENT WHEN INVENTORY IS ADDED
        return true;
    }

    /**
     * Block hardness will be further counted in {@link Block#getPlayerRelativeBlockHardness}
     */
    public float getToolDigEfficiency(@NotNull Block blockToHarvest) {
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
    public SkinBase getSkin() {
        return skin;
    }
}
