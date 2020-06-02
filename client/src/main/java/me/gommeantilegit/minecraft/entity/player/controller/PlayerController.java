package me.gommeantilegit.minecraft.entity.player.controller;

import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.block.sound.BlockSoundType;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PlayerController {

    /**
     * Block damage.
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

    /**
     * Parent client minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    public PlayerController(@NotNull EntityPlayerSP player, @NotNull ClientMinecraft mc) {
        this.player = player;
        this.mc = mc;
    }

    /**
     * The block position that the player is breaking. Stores a value of null, if the player is not breaking any block.
     */
    @Nullable
    private BlockPos breakingPos = null;

    /**
     * Updating the damage applied to the block which the player faces.
     */
    public void onPlayerBlockDamage() {
        if (blockHitDelay > 0) {
            blockHitDelay--;
            return;
        }
        RayTracer.RayTraceResult result = player.rayTracer.getRayTraceResult();
        if (result == null) return;
        BlockPos pos = result.getBlockPos();
        if (pos != null) {
            assert result.hitSide != null;
            if (player.isBreakingBlocks() && (breakingPos == null || pos.equals(breakingPos))) {
                player.swingItem();

                breakingPos = pos;
                if (result.type != RayTracer.RayTraceResult.EnumResultType.BLOCK) {
                    return;
                }
                Block block = player.getWorld().getBlock(pos);
                if (block != null) {
                    this.currentBlockDamage += block.getPlayerRelativeBlockHardness(player, player.getWorld(), pos);
                    if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                        BlockSoundType soundType = mc.blockSounds.getSoundType(block);
                        // Playing block breaking sound
//                        soundType.getStepSound().play((soundType.getVolume() + 1) / 8.0f, soundType.pitch * 0.5f);
                        soundType.getStepSound().playRelative((soundType.getVolume() + 1) / 8.0f, soundType.pitch * 0.5f, Objects.requireNonNull(result.hitVec), mc.thePlayer.getUpdatedPositionVector(), mc.thePlayer.camera.direction);
                    }
                    this.stepSoundTickCounter++;
                    if (currentBlockDamage >= 1f) {
                        BlockSoundType soundType = mc.blockSounds.getSoundType(block);
                        soundType.getStepSound().playRelative((soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, Objects.requireNonNull(result.hitVec), mc.thePlayer.getUpdatedPositionVector(), mc.thePlayer.camera.direction);
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
    public void onBlockBreakingAbort() {
        this.currentBlockDamage = 0;
        this.stepSoundTickCounter = 0;
        this.blockHitDelay = 0;
    }
}
