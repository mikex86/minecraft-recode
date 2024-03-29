package me.gommeantilegit.minecraft.entity.living;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.utils.MathHelper;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.lang.Math.*;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

public class LivingEntity extends Entity {


    /**
     * State whether the entity is sprinting.
     */
    private boolean sprinting;

    /**
     * forward and strafing movement inputs
     */
    public float moveForward = 0, moveStrafing = 0;

    /**
     * State if the entity is jumping
     */
    private boolean jumping = false;

    /**
     * The overall speed constant of the entity in air
     */
    private final float speedInAir = 0.02f;

    /**
     * The overall speed constant of the entity on ground
     */
    private final float speedOnGround = 0.1f;

    /**
     * Speed in air and on ground that the player is moved with
     */
    private float currentGroundSpeed, currentAirSpeed;

    /**
     * Only relevant when limbYaw is not 0(the entity is moving). Influences where in its swing legs and arms currently
     * are.
     */
    public float prevLimbSwing, limbSwing;

    /**
     * Limb swing intensity
     */
    public float prevLimbSwingAmount, limbSwingAmount;

    /**
     * Whether an arm swing is currently in progress.
     */
    public boolean isSwingInProgress;

    /**
     * Swing progress as integer
     */
    public int swingProgressInt;

    /**
     * Swing progress
     */
    public float swingProgress;

    /**
     * Swing progress of last tick
     */
    public float prevSwingProgress;

    /**
     * Yaw Head offset
     */
    public float prevRenderYawOffset, renderYawOffset;

    /**
     * Amount of swings performed
     */
    private int swings = 0;

    /**
     * Health of the entity.
     */
    private int health;

    /**
     * The time the entity is resistant to further hurt in ticks.
     */
    private int hurtResistanceTime;

    /**
     * Stores the maximum health of the entity.
     */
    private int maxHealth;

    /**
     * Stores the time the entity is still going to be hurt in ticks. Zero if not hurt, 10 if just hurt.
     */
    private int hurtTime;

    public float prevCameraYaw;
    public float cameraYaw;

    public float prevCameraPitch;
    public float cameraPitch;

    /**
     * Clock instance used for timing of regeneration.
     */
    private final Clock healTimer = new Clock(false);

    /**
     * @param world     sets {@link #world}
     * @param maxHealth sets {@link #maxHealth}
     */
    public LivingEntity(@Nullable WorldBase world, int maxHealth) {
        super(world);
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
    }

    @Override
    public void tick() {
        super.tick();
        onLivingUpdate();
        updateLimbSwing();
        updateYawOffset();
        updateArmSwingProgress();
        updateCameraAngles();
    }

    private void updateCameraAngles() {
        this.prevCameraYaw = this.cameraYaw;
        this.prevCameraPitch = this.cameraPitch;

        float f1 = (float) Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float f = (float) Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F; // this weired constant again

        if (f1 > 0.1F) {
            f1 = 0.1F;
        }

        if (!this.isOnGround() || this.getHealth() <= 0.0F) {
            f1 = 0.0F;
        }

        if (this.isOnGround() || this.getHealth() <= 0.0F) {
            f = 0.0F;
        }

        this.cameraYaw += (f1 - this.cameraYaw) * 0.4F;
        this.cameraPitch += (f - this.cameraPitch) * 0.8F;
    }

    /**
     * Subtracts the given amount of health of the players health
     *
     * @param amount the amount to be subtracted
     */
    public void hurt(int amount) {
        if (hurtResistanceTime == 0) {
            setHealth(getHealth() - amount);
        }
    }

    /**
     * Regeneration update
     */
    protected void regenerate() {
        if (hurtResistanceTime > 0)
            hurtResistanceTime--;

        if (hurtTime > 0)
            hurtTime--;

        if (this.getHealth() < getMaxHealth() && hurtTime == 0) {
            if (healTimer.getTimePassed() > 1500) {
                this.healHeart();
                healTimer.reset();
            }
        }
    }

    /**
     * Fills up live by a half heart
     */
    private void healHeart() {
        this.setHealth(getHealth() + 1);
        this.hurtResistanceTime = 5;
    }

    /**
     * Sets the health of the entity. Protects from setting to to big or negative values.
     *
     * @param health the new amount of health
     */
    public void setHealth(int health) {
        this.hurtTime = 10;
        this.hurtResistanceTime = 10;
        this.health = Math.max(0, Math.min(health, 20));
    }

    /**
     * Called frequently so the entity can update its state every tick as required.
     */
    public void onLivingUpdate() {
        this.motionX *= 0.98;
        this.motionY *= 0.98;
        this.motionZ *= 0.98;

        if (abs(this.motionX) < 0.015) {
            this.motionX = 0.0f;
        }

        if (abs(this.motionY) < 0.015) {
            this.motionY = 0.0f;
        }

        if (abs(this.motionZ) < 0.015D) {
            this.motionZ = 0.0f;
        }
        if (jumping && isOnGround())
            jump();

        //TRAVELLING
        {
            this.moveStrafing *= 0.98F;
            this.moveForward *= 0.98F;
            this.moveEntityWithHeading(this.moveStrafing, this.moveForward);
        }
    }

    /**
     * Updating render-yaw offset (Body turn)
     */
    private void updateYawOffset() {
        prevRenderYawOffset = renderYawOffset;

        float offset;
        {
            double dx = posX - lastPosX;
            double dz = posZ - lastPosZ;
            float distance = (float) sqrt(dx * dx + dz * dz);
            float angle;
            boolean isMoving = distance > 0.03f;
            if (isMoving) {
                offset = (float) toDegrees(atan2(dx, dz)) - 90;
            } else {
                offset = renderYawOffset;
            }
            if (this.swingProgress > 0.0F) {
                offset = this.rotationYaw;
            }
            if (isMoving) {
                angle = MathHelper.wrapToAngle((rotationYaw + 90) - offset - this.renderYawOffset);
                float bound = 75;
                this.renderYawOffset += angle * 0.2F;
                renderYawOffset = max(-bound, min(renderYawOffset, bound));
            } else {
                float yawDif = this.lastRotationYaw - this.rotationYaw;
                this.renderYawOffset += yawDif;

                if (yawDif != 0) {
                    if (abs(this.renderYawOffset) > 25) {
                        this.renderYawOffset = 0;
                    }
                }
            }
        }
        while (this.rotationYaw - this.lastRotationYaw < -180.0F) {
            this.lastRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.lastRotationYaw >= 180.0F) {
            this.lastRotationYaw += 360.0F;
        }

        while (this.renderYawOffset - this.prevRenderYawOffset < -180.0F) {
            this.prevRenderYawOffset -= 360.0F;
        }

        while (this.renderYawOffset - this.prevRenderYawOffset >= 180.0F) {
            this.prevRenderYawOffset += 360.0F;
        }

        while (this.rotationPitch - this.lastRotationPitch < -180.0F) {
            this.lastRotationPitch -= 360.0F;
        }

        while (this.rotationPitch - this.lastRotationPitch >= 180.0F) {
            this.lastRotationPitch += 360.0F;
        }
    }

    /**
     * Updates the limb swing amount. For player animation
     */
    private void updateLimbSwing() {
        prevLimbSwing = limbSwing;
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double xMove = this.posX - this.lastPosX;
        double zMove = this.posZ - this.lastPosZ;
        float amount = (float) (sqrt(xMove * xMove + zMove * zMove) * 4.0F);
        amount = min(amount, 1f);
        this.limbSwingAmount += (amount - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    public void jump() {
        //TODO: FINISH JUMP METHOD
        //if (this.isInFluid()) {
        //FLUID MODIFIED MOTION Y VALUE (for lava: 0.03999999910593033f)
        //} else {
        this.motionY = this.getJumpUpwardsMotion();

        if (this.isSprinting()) {
            float speed = 0.2f;
            float jumpDir = (float) toRadians(this.rotationYaw - 180);
            this.motionX += (double) (sin(jumpDir) * speed * moveForward);
            this.motionZ += (double) (cos(jumpDir) * speed * moveForward);
        }
        // }
    }

    /**
     * @return the motionY that should be applied to the player's motion when jumping
     */
    private float getJumpUpwardsMotion() {
        return 0.42F;
    }

    /**
     * Swings the item the player is holding.
     */
    public void swingItem() {
        if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
            this.swingProgressInt = -1;
            this.isSwingInProgress = true;
        }
        this.swings++;
    }

    /**
     * @return where in the swing animation the living entity is (from 0 to 1).  Args: partialTickTime
     */
    public float getInterpolatedSwingProgress(float partialTickTime) {
        float f = this.swingProgress - this.prevSwingProgress;
        if (f < 0.0F) {
            ++f;
        }
        return this.prevSwingProgress + f * partialTickTime;
    }

    /**
     * @return an integer indicating the end point of the swing animation, used by {@link #swingProgress} to provide a
     * progress indicator.
     */
    private int getArmSwingAnimationEnd() {
        return 6;
    }

    /**
     * Updates the arm swing progress counters and animation progress
     */
    public void updateArmSwingProgress() {
        this.prevSwingProgress = this.swingProgress;
        int i = this.getArmSwingAnimationEnd();

        if (this.isSwingInProgress) {
            this.swingProgressInt++;
            if (this.swingProgressInt >= i) {
                this.swingProgressInt = 0;
                this.isSwingInProgress = false;
            }
        } else {
            this.swingProgressInt = 0;
        }

        this.swingProgress = (float) this.swingProgressInt / (float) i;
    }

    /**
     * Moves the entity based on the specified heading
     *
     * @param strafe  strafing movement
     * @param forward forward movement
     */
    public void moveEntityWithHeading(float strafe, float forward) {
        //TODO: FINISH AFTER IMPLEMENTING FLUID SYSTEM
        // if (this.isInFluid()) {
        //FLUID BASE MOTION ALTERING
        // } else {
        moveEntityInAir(strafe, forward);
        // }
    }

    public void moveEntityInAir(float moveStrafing, float moveForward) {
        if (this.currentChunk == null) {
            return;
        }

        float friction = 0.91F; // Default friction in air


        IBlockState verticalCollision;

        {
            ChunkBase chunk = this.world.getNearChunkFor(this.currentChunk, (int) floor(this.posX), (int) floor(this.posZ));
            Objects.requireNonNull(chunk, "Could not find chunk for int floor x, z (vertical collision) " + this.posX + ", " + this.posZ);
            verticalCollision = chunk.getBlockState((int) floor(this.posX), (int) floor(this.getBoundingBox().y0) - 1, (int) floor(this.posZ));
        }
        if (this.isOnGround() && verticalCollision != null) {
            friction = verticalCollision.getBlock().getSlipperiness() * 0.91f; // friction against vertically colliding block
        } else if (isOnGround()) {
            friction = 0.54600006f; // Friction for air is 0.6 * 0.91f (eg if standing on another block but due to rounding the block below is air)
        }

        float frictionalValue = 0.16277136F / (friction * friction * friction);
        float speed;

        {
            this.currentAirSpeed = speedInAir; // Default speed in air
            this.currentGroundSpeed = this.speedOnGround;
            if (isSprinting()) {
                this.currentAirSpeed = this.currentAirSpeed + this.speedInAir * 0.3f;
                this.currentGroundSpeed = this.currentGroundSpeed + this.speedOnGround * 0.3f;
            }
        }

        if (this.isOnGround()) {
            speed = this.currentGroundSpeed * frictionalValue;
        } else {
            speed = this.currentAirSpeed;
        }

        updateMotion(moveStrafing, moveForward, speed);

        moveEntity(motionX, motionY, motionZ);

        this.motionY -= 0.08D; // GRAVITY
        this.motionY *= 0.9800000190734863D;
        this.motionX *= friction;
        this.motionZ *= friction;
    }

    /**
     * @return true if the entity should be rendered
     */
    public boolean isVisible() {
        return true;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public float getCurrentAirSpeed() {
        return currentAirSpeed;
    }

    public float getCurrentGroundSpeed() {
        return currentGroundSpeed;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getHurtResistanceTime() {
        return hurtResistanceTime;
    }

    public int getHealth() {
        return health;
    }

    public int getHurtTime() {
        return hurtTime;
    }

    public void setHurtTime(int hurtTime) {
        this.hurtTime = hurtTime;
    }

}
