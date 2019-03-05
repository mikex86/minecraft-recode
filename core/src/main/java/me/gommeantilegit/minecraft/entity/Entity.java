package me.gommeantilegit.minecraft.entity;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.util.Clock;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static java.lang.Math.*;

public class Entity {

    /**
     * Motion components of the entity's motion of last tick.
     */
    public float lastMotionX, lastMotionY, lastMotionZ;

    /**
     * Rotation component of the entity updated every tick.
     */
    public float rotationYawTicked, rotationPitchTicked;

    /**
     * The amount of ticks the entity should skip until being updated again.
     */
    public int skipUpdateTicks;

    /**
     * Amount of ticks the entity has existed
     */
    public int ticksExisted;

    /**
     * World that the entity is in.
     */
    protected World world;

    /**
     * Position components of the entity's position of last tick.
     */
    public float lastPosX, lastPosY, lastPosZ;
    /**
     * Position components of the entity's current position. (Ticked)
     */
    public float posX, posY, posZ;

    /**
     * Motion components of the entity's current motion. (Ticked)
     */
    public float motionX, motionY, motionZ;

    /**
     * Rotation components of the entity's current rotation. (not necessarily ticked)
     */
    public float rotationYaw, rotationPitch;

    /**
     * Rotation components ot the entity's last rotation. (Last tick)
     */
    public float lastRotationYaw, lastRotationPitch;

    /**
     * Bounding box / Hitbox of the entity.
     */
    public AxisAlignedBB boundingBox;

    /**
     * State if the entity is currently on ground. Updated on tick.
     */
    public boolean onGround = false;

    /**
     * State if the entity is collided with a block on the horizontal axis.
     */
    public boolean collidedHorizontally = false;

    /**
     * State if the entity is dead. (Entity will be removed of the world if true in the next tick)
     */
    public boolean dead = false;

    protected float heightOffset = 0.0f;

    /**
     * Width of the bounding box.
     */
    protected float bbWidth = 0.6f;

    /**
     * Height of the bounding box.
     */
    protected float bbHeight = 1.8f;

    /**
     * Stores the tick that the entity was last updated on.
     */
    public long lastUpdated;

    /**
     * State if the entity can be collided with.
     */
    private boolean collidable = false;

    /**
     * @param world     sets {@link #world}
     */
    public Entity(World world) {
        this.world = world;
        this.resetPos();
    }

    /**
     * Sets the entity's position randomly.
     */
    public void resetPos() {
        if (world != null) {
            float x = 0;
            float y = 40;
            float z = 0;
            setPosition(x, y, z);
        } else {
            setPosition(0, 0, 0);
        }
    }

    /**
     * @return a new position vector equal to vec3({@link #posX}, {@link #posY}, {@link #posZ})
     * Please note, that changes made to this instance do not affect the instance in any way.
     */
    public Vector3 getPositionVector() {
        return new Vector3(posX, posY, posZ);
    }

    /**
     * Sets the state of the entity being dead to true resulting in the entity being removed out of the world in the next tick.
     */
    public void setDead() {
        this.dead = true;
    }

    /**
     * Sets the size variables {@link #bbWidth} and {@link #bbHeight} to the given values.
     *
     * @param width  new width
     * @param height new height
     */
    protected void setSize(float width, float height) {
        this.bbWidth = width;
        this.bbHeight = height;
    }

    /**
     * Sets the position of the entity to the specified coordinates and updates it's bounding box.
     *
     * @param x x position component.
     * @param y y position component.
     * @param z z position component.
     */
    public void setPosition(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float w = this.bbWidth / 2.0f;
        float h = this.bbHeight / 2.0f;
        this.boundingBox = new AxisAlignedBB(x - w, y - h, z - w, x + w, y + h, z + w);
    }

    /**
     * Called when the entity has moved to another chunk. Called before the switch is performed.
     *
     * @param newChunk the new chunk
     * @param oldChunk the old chunk
     */
    public void onChunkChanged(@NotNull Chunk newChunk, @NotNull Chunk oldChunk) {

    }

    /**
     * Modifies the entities view by the given parameters
     *
     * @param yaw   rotationYaw gain
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

    /**
     * Called on entity death (when the entity is removed out of the world when it's dead)
     * The entity is still in the entity list of it's chunk when the method is called but removed immediately after this method has been called.
     */
    public void onDeath() {
    }

    /**
     * Entity tick update
     */
    public void tick() {
        this.ticksExisted++;
        lastRotationYaw = rotationYawTicked;
        lastRotationPitch = rotationPitchTicked;
        rotationYawTicked = rotationYaw;
        rotationPitchTicked = rotationPitch;
        this.lastPosX = this.posX;
        this.lastPosY = this.posY;
        this.lastPosZ = this.posZ;
        lastMotionX = motionX;
        lastMotionY = motionY;
        lastMotionZ = motionZ;

        //TODO: FIX
//        ArrayList<AxisAlignedBB> boundingBoxes = this.world.getBoundingBoxes(this.boundingBox.expand(-0.05f, -0.05f, -0.05f));
//        for (AxisAlignedBB abb : boundingBoxes)
//            this.world.collision(boundingBoxes, this, abb);
    }

    public boolean isFree(float xa, float ya, float za) {
        AxisAlignedBB box = this.boundingBox.cloneMove(xa, ya, za);
        ArrayList<AxisAlignedBB> aABBS = this.world.getBoundingBoxes(box);
        return aABBS.size() <= 0;
    }

    /**
     * Applies player physics to the motion of the player (gravity and collision checks)
     * and applies the motion to the player by altering it's position accordingly
     *
     * @param motionX motionX of the entity
     * @param motionY motionY of the entity
     * @param motionZ motionZ of the entity
     */
    public void moveEntity(float motionX, float motionY, float motionZ) {
        float xNew = motionX;
        float yNew = motionY;
        float zNew = motionZ;

        // SNEAKING (SAFEWALK)
        {
            boolean flag = this.onGround && this instanceof PlayerBase && ((PlayerBase)this).isSneaking();

            if (flag) {
                double d6;

                for (d6 = 0.05D; xNew != 0.0D && this.world.isFree(this.getBoundingBox().cloneMove(xNew, -1.0f, 0.0f)); ) {
                    if (xNew < d6 && xNew >= -d6) {
                        xNew = 0.0f;
                    } else if (xNew > 0.0D) {
                        xNew -= d6;
                    } else {
                        xNew += d6;
                    }
                }

                for (; zNew != 0.0D && this.world.isFree(this.getBoundingBox().cloneMove(0.0f, -1.0f, zNew)); ) {
                    if (zNew < d6 && zNew >= -d6) {
                        zNew = 0.0f;
                    } else if (zNew > 0.0D) {
                        zNew -= d6;
                    } else {
                        zNew += d6;
                    }
                }

                for (; xNew != 0.0D && zNew != 0.0D && this.world.isFree(this.getBoundingBox().cloneMove(xNew, -1.0f, zNew)); ) {
                    if (xNew < d6 && xNew >= -d6) {
                        xNew = 0.0f;
                    } else if (xNew > 0.0D) {
                        xNew -= d6;
                    } else {
                        xNew += d6;
                    }

                    if (zNew < d6 && zNew >= -d6) {
                        zNew = 0.0f;
                    } else if (zNew > 0.0D) {
                        zNew -= d6;
                    } else {
                        zNew += d6;
                    }
                }
            }
        }
        //PHYSICS
        {
            ArrayList<AxisAlignedBB> aABBS = this.world.getBoundingBoxes(this.boundingBox.expand(motionX, motionY, motionZ));
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

        }
        this.boundingBox.move(0.0f, 0.0f, motionZ);

        this.collidedHorizontally = xNew != motionX || zNew != motionZ;
        this.onGround = yNew != motionY && yNew < 0.0f;
        if (xNew != motionX) {
            this.motionX = 0.0f;
        }
        if (yNew != motionY) {
            this.motionY = 0.0f;
        }
        if (zNew != motionZ) {
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
     * Updates the players motion components
     *
     * @param forward the new forward movement value
     * @param strafe  the new strafing movement value
     * @param speed   movement speed
     */
    protected void updateMotion(float forward, float strafe, float speed) {
        float dist = strafe * strafe + forward * forward;

        if (dist >= 1.0E-4F) {
            dist = (float) sqrt(dist);

            if (dist < 1.0F) {
                dist = 1.0F;
            }

            dist = speed / dist;
            strafe = strafe * dist;
            forward = forward * dist;
            float sin = (float) sin(this.rotationYaw * (float) PI / 180.0F);
            float cos = (float) cos(this.rotationYaw * (float) PI / 180.0F);
            this.motionZ -= (double) (strafe * cos - forward * sin);
            this.motionX -= (double) (forward * cos + strafe * sin);
//            this.motionX += (forward *= dist) * sin - (strafe *= dist) * cos;
//            this.motionZ += strafe * sin + forward * cos;
        }
    }

    /**
     * @return true if the entity is swimming in a fluid
     */
    protected boolean isInFluid() {
        //TODO: IMPLEMENT AFTER IMPLEMENTING FLUID SYSTEM
        return false;
    }

    /**
     * Renders the given Entity
     *
     * @param partialTicks ticks performed this frame
     */
    public void render(float partialTicks) {
    }

    /**
     * @param chunk the given chunk
     * @return the state if the entity is in the given chunk.
     */
    public boolean isInChunk(@NotNull Chunk chunk) {
        return chunk.contains((int) posX, (int) posZ);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Called to check if the entity allows the scheduleUnload of it's chunk.
     *
     * @param chunk the chunk that the entity is in.
     */
    public boolean allowChunkUnload(Chunk chunk) {
        return true;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public float getCollisionBorderSize() {
        return 0.1F;
    }

    public boolean canBeCollidedWith() {
        return this.collidable;
    }

    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }
}
