package me.gommeantilegit.minecraft.entity;

import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.util.Clock;
import me.gommeantilegit.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Entity {

    public float lastMotionX, lastMotionY, lastMotionZ;
    public float rotationYawTicked, rotationPitchTicked;
    protected World world;
    public float lastPosX;
    public float lastPosY;
    public float lastPosZ;
    public float posX;
    public float posY;
    public float posZ;
    public float motionX;
    public float motionY;
    public float motionZ;
    public float rotationYaw;
    public float rotationPitch;
    public float lastRotationYaw;
    public float lastRotationPitch;
    public AxisAlignedBB boundingBox;
    public boolean onGround = false;
    public boolean horizontalCollision = false;
    public boolean dead = false;
    protected float heightOffset = 0.0f;
    protected float bbWidth = 0.6f;
    protected float bbHeight = 1.8f;
    private boolean sprinting;
    private int health;
    private int hurtResistanceTime;
    private int maxHealth;
    private int hurtTime;

    public Entity(World world, int maxHealth) {
        this.world = world;
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
        this.resetPos();

    }

    public void resetPos() {
        if (world != null) {
            float x = (float) Math.random() * (float) (this.world.getWidth() - 2) + 1.0f;
            float y = 40;
            float z = (float) Math.random() * (float) (this.world.getDepth() - 2) + 1.0f;
            setPos(x, y, z);
        } else {
            setPos(0, 0, 0);
        }
    }

    public void remove() {
        this.dead = true;
    }

    protected void setSize(float w, float h) {
        this.bbWidth = w;
        this.bbHeight = h;
    }

    protected void setPos(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float w = this.bbWidth / 2.0f;
        float h = this.bbHeight / 2.0f;
        this.boundingBox = new AxisAlignedBB(x - w, y - h, z - w, x + w, y + h, z + w);
    }

    public World getWorld() {
        return world;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    private final Random random = new Random();

    private final Clock clock = new Clock(false);

    /**
     * Regeneration update
     */
    protected void regenerate() {
        if (hurtResistanceTime > 0)
            hurtResistanceTime--;

        if (hurtTime > 0)
            hurtTime--;

        if (this.getHealth() < getMaxHealth() && hurtTime == 0) {
            if (clock.getTimePassed() > 1500) {
                this.healHeart();
                clock.reset();
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
     * Modifies the entities view by the given parameters
     * @param yaw rotationYaw gain
     * @param pitch rotationPitch gain
     */
    public void turn(double yaw, double pitch) {
        this.rotationYaw = (float) ((double) this.rotationYaw + yaw * 0.15);
        this.rotationPitch = (float) ((double) this.rotationPitch - pitch * 0.15);
        if (this.rotationPitch < -90.0f) {
            this.rotationPitch = -90.0f;
        }
        if (this.rotationPitch > 90.0f) {
            this.rotationPitch = 90.0f;
        }
    }

    public int getHurtResistanceTime() {
        return hurtResistanceTime;
    }

    /**
     * Entity tick update
     */
    public void tick() {
        rotationYawTicked = rotationYaw;
        rotationPitchTicked = rotationPitch;
        this.lastPosX = this.posX;
        this.lastPosY = this.posY;
        this.lastPosZ = this.posZ;
        lastMotionX = motionX;
        lastMotionY = motionY;
        lastMotionZ = motionZ;

        ArrayList<AxisAlignedBB> boundingBoxes = this.world.getCubes(this.boundingBox.expand(-0.05f, -0.05f, -0.05f));
        for (AxisAlignedBB abb : boundingBoxes)
            this.world.collision(boundingBoxes, this, abb);


        lastRotationYaw = rotationYaw;
        lastRotationPitch = rotationPitch;
    }

    public boolean isFree(float xa, float ya, float za) {
        AxisAlignedBB box = this.boundingBox.cloneMove(xa, ya, za);
        ArrayList<AxisAlignedBB> aABBS = this.world.getCubes(box);
        return aABBS.size() <= 0;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    /**
     * Moves the entity around
     * @param motionX motionX of the entity
     * @param motionY motionY of the entity
     * @param motionZ motionZ of the entity
     */
    public void move(float motionX, float motionY, float motionZ) {
        float xaOrg = motionX;
        float yaOrg = motionY;
        float zaOrg = motionZ;
        ArrayList<AxisAlignedBB> aABBS = this.world.getCubes(this.boundingBox.expand(motionX, motionY, motionZ));
        int i = 0;
        while (i < aABBS.size()) {
            motionY = aABBS.get(i).clipYCollide(this.boundingBox, motionY);
            ++i;
        }
        this.boundingBox.move(0.0f, motionY, 0.0f);
        i = 0;
        while (i < aABBS.size()) {
            motionX = aABBS.get(i).clipXCollide(this.boundingBox, motionX);
            ++i;
        }
        this.boundingBox.move(motionX, 0.0f, 0.0f);
        i = 0;
        while (i < aABBS.size()) {
            motionZ = aABBS.get(i).clipZCollide(this.boundingBox, motionZ);
            ++i;
        }
        this.boundingBox.move(0.0f, 0.0f, motionZ);
        this.horizontalCollision = xaOrg != motionX || zaOrg != motionZ;
        boolean bl = this.onGround = yaOrg != motionY && yaOrg < 0.0f;
        if (xaOrg != motionX) {
            this.motionX = 0.0f;
        }
        if (yaOrg != motionY) {
            this.motionY = 0.0f;
        }
        if (zaOrg != motionZ) {
            this.motionZ = 0.0f;
        }
        this.posX = (this.boundingBox.x0 + this.boundingBox.x1) / 2.0f;
        this.posY = this.boundingBox.y0 + this.heightOffset;
        this.posZ = (this.boundingBox.z0 + this.boundingBox.z1) / 2.0f;
    }

    //TODO
//    public boolean isInWater() {
//        return this.world.getBlocksIn(this.boundingBox.grow(0.0f, -0.4f, 0.0f)).contains();
//    }

    //TODO
//    public boolean isInLava() {
//        return this.world.containsLiquid(this.boundingBox, 2);
//    }

    /**
     * Updates motion of the entity according to the new motion values specified as parameters.
     * @param newMotionX the new motionX value
     * @param newMotionZ the new motionZ value
     * @param speed movement speed
     */
    public void moveRelative(float newMotionX, float newMotionZ, float speed) {
        float dist = newMotionX * newMotionX + newMotionZ * newMotionZ;
        if (dist < 0.01f) {
            return;
        }
        dist = speed / (float) Math.sqrt(dist);
        float sin = (float) Math.sin((double) this.rotationYaw * 3.141592653589793 / 180.0);
        float cos = (float) Math.cos((double) this.rotationYaw * 3.141592653589793 / 180.0);
        this.motionX += (newMotionX *= dist) * cos - (newMotionZ *= dist) * sin;
        this.motionZ += newMotionZ * cos + newMotionX * sin;
    }

    /**
     * Renders the given Entity
     * @param partialTicks delta time
     */
    public void render(float partialTicks) {
    }

    /**
     * Sets the health of the entity. Protects from setting to to big or negative values.
     * @param health the new amount of health
     */
    public void setHealth(int health) {
        this.hurtTime = 10;
        this.hurtResistanceTime = 10;
        this.health = max(0, min(health, 20));
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

    /**
     * Subtracts the given amount of health of the players health
     * @param amount the amount to be subtracted
     */
    public void hurt(int amount) {
        if (hurtResistanceTime == 0) {
            setHealth(getHealth() - amount);
        }
    }
}
