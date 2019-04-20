package me.gommeantilegit.minecraft.entity.player;

import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PlayerController {

    /**
     * BlockBase damage.
     */
    public float currentBlockDamage = 0f;

    /**
     * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
     */
    float stepSoundTickCounter = 0;

    /**
     * The parent player being controlled
     */
    @NotNull
    private final EntityPlayerSP player;

    /**
     * Delays the first damage on the block after the first click on the block
     */
    private int blockHitDelay;

    public PlayerController(@NotNull EntityPlayerSP player) {
        this.player = player;
    }

    /**
     * The block position that the player is breaking. Stores a value of null, if the player is not breaking any block.
     */
    @Nullable
    private BlockPos breakingPos = null;

    /**
     * Updating the damage applied to the block which the player faces.
     */
    void onPlayerBlockDamage() {
        if (blockHitDelay > 0) {
            blockHitDelay--;
            return;
        }
        RayTracer.RayTraceResult result = player.rayTracer.getRayTraceResult();
        if (result == null) return;
        BlockPos pos = result.getBlockPos();
        if (pos != null) {
            assert result.hitSide != null;
            if (player.breakingBlocks && (breakingPos == null || pos.equals(breakingPos))) {
                player.swingItem();

                breakingPos = pos;
                if (result.type != RayTracer.RayTraceResult.EnumResultType.BLOCK) {
                    return;
                }
                BlockBase block = player.getWorld().getBlock(pos);
                if (block != null) {
                    this.currentBlockDamage += block.getPlayerRelativeBlockHardness(player, player.getWorld(), pos);
                    if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                        //TODO: IMPLEMENT PLAY DIG SOUND
                    }
                    this.stepSoundTickCounter++;
                    if (currentBlockDamage >= 1f) {
                        player.onBlockBroken(block, pos, result.hitSide);
                        currentBlockDamage = 0f;
                        stepSoundTickCounter = 0f;
                        blockHitDelay = 5;
                    }
                }
            } else {
                onBlockBreakingAbort();
                breakingPos = null;
            }
        }
    }

    /**
     * Called when the player aborts block breaking
     */
    void onBlockBreakingAbort() {
        this.currentBlockDamage = 0;
        this.stepSoundTickCounter = 0;
        this.blockHitDelay = 0;
    }
}
