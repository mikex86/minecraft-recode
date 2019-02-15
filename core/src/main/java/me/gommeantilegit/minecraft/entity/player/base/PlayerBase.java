package me.gommeantilegit.minecraft.entity.player.base;

import me.gommeantilegit.minecraft.entity.living.LivingEntity;
import me.gommeantilegit.minecraft.world.World;

public class PlayerBase extends LivingEntity {

    /**
     * State whether the player is sneaking
     * //TODO: IMPLEMENT
     */
    private boolean sneaking = false;

    /**
     * @param world     sets {@link #world}
     * @param maxHealth sets {@link #maxHealth}
     */
    public PlayerBase(World world, int maxHealth) {
        super(world, maxHealth);
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public PlayerBase setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
        return this;
    }
}
