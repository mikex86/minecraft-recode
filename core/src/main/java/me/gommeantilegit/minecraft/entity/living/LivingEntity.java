package me.gommeantilegit.minecraft.entity.living;

import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.util.MathHelper;
import me.gommeantilegit.minecraft.world.World;

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
     * @param world     sets {@link #world}
     * @param maxHealth sets {@link #maxHealth}
     */
    public LivingEntity(World world, int maxHealth) {
        super(world, maxHealth);
    }

    @Override
    public void tick() {
        super.tick();
        onLivingUpdate();
        updateLimbSwing();
        updateYawOffset();
        updateArmSwingProgress();
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
        if (jumping && onGround)
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

        float offset = renderYawOffset;
        {
            double dx = posX - lastPosX;
            double dz = posZ - lastPosZ;
            float distance = (float) sqrt(dx * dx + dz * dz);

            if (distance > 0.03f)
                offset = (float) toDegrees(atan2(dx, dz)) - 180;


            if (this.swingProgress > 0.0F) {
                offset = this.rotationYaw;
            }

            float bound = 75;
            float angle = MathHelper.wrapToAngle(rotationYaw - offset - this.renderYawOffset);
            this.renderYawOffset += angle * 0.2F;
            renderYawOffset = max(-bound, min(renderYawOffset, bound));
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
        // if (this.isInFluid()) {
        // FLUID MODIFIED MOTION Y VALUE (for lava: 0.03999999910593033f)
        // } else {
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
        float friction = 0.91F; // Default friction in air


        IBlockState verticalCollision = this.world.getBlockState((int) floor(this.posX), (int) floor(this.getBoundingBox().y0) - 1, (int) floor(this.posZ));

        if (this.onGround && verticalCollision != null && verticalCollision.getBlock() != null) {
            friction = verticalCollision.getBlock().getSlipperiness() * 0.91f; // friction against vertically colliding block
        } else if(onGround){
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

        if (this.onGround) {
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
}
