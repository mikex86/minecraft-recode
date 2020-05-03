package me.gommeantilegit.minecraft.entity;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.WorldBase;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

//    /**
//     * The amount of ticks the entity should skip until being updated again.
//     */
//    public int skipUpdateTicks;

    /**
     * Amount of ticks the entity has existed
     */
    public long ticksExisted;

    /**
     * ServerWorld that the entity is in.
     */
    public WorldBase world;

    private long tickJoinMark = -1;

    /**
     * The chunk that the entity is currently in.
     */
    protected ChunkBase currentChunk;

    /**
     * Object for making sure that the entity render position and the camera position are synced as the tick happens asynchronous
     */
    @NotNull
    @SideOnly(side = Side.CLIENT)
    protected final EntityRenderPosition entityRenderPosition = new EntityRenderPosition();

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

    private float prevDistanceWalkedModified;

    private float distanceWalkedModified;

    private float distanceWalkedOnStepModified;

    /**
     * The distance that has to be exceeded in order to trigger a new step sound and an onEntityWalking event on a block
     */
    private int nextStepDistance = 1;

    private AxisAlignedBB boundingBox;

    private boolean onGround = false;

    private boolean collidedHorizontally = false;

    private boolean dead = false;

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
     * State whether the entity can be collided with.
     */
    private boolean collidable = false;

    /**
     * The chunk that the entity is transferred to. (Nullified when change is complete)
     */
    @Nullable
    private ChunkBase newChunk = null;

    /**
     * @param world sets {@link #world}
     */
    public Entity(@Nullable WorldBase world) {
        this.world = world;
        float w = this.bbWidth / 2.0f;
        float h = this.bbHeight / 2.0f;
        this.setBoundingBox(new AxisAlignedBB(posX - w, posY - h, posZ - w, posX + w, posY + h, posZ + w));
    }

    /**
     * @return a new position vector equal to vec3({@link #posX}, {@link #posY}, {@link #posZ})
     * Please note, that changes made to this instance do not affect the instance in any way.
     */
    @NotNull
    public Vector3 getPositionVector() {
        return new Vector3(posX, posY, posZ);
    }

    /**
     * Sets the state of the entity being dead to true resulting in the entity being removed out of the world in the next tick.
     */
    public void setDead() {
        this.setDead(true);
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
        this.getBoundingBox().set(x - w, y - h, z - w, x + w, y + h, z + w);
    }

    /**
     * Called when the entity has moved to another chunk. Called before the switch is performed.
     *
     * @param newChunk the new chunk
     * @param oldChunk the old chunk. Can be null on spawn.
     */
    public void onChunkChanged(@NotNull ChunkBase newChunk, @Nullable ChunkBase oldChunk) {
        this.setCurrentChunk(newChunk);
        this.world.getChunkLoader().trackEntityChunkChange(this, newChunk, oldChunk);
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

        setEntityRenderPosition();

        //TODO: FIX?
        List<AxisAlignedBB> boundingBoxes = this.world.getBoundingBoxes(this.getBoundingBox().expand(-0.05f, -0.05f, -0.05f));
        for (AxisAlignedBB abb : boundingBoxes)
            this.world.collision(boundingBoxes, this, abb);
    }

    /**
     * Called to adjust {@link #entityRenderPosition}
     */
    @SideOnly(side = Side.CLIENT)
    protected void setEntityRenderPosition() {
        this.entityRenderPosition.set(
                lastPosX, lastPosY, lastPosZ,
                posX, posY, posZ
        );

    }

    public boolean isFree(float xa, float ya, float za) {
        AxisAlignedBB box = this.getBoundingBox().cloneMove(xa, ya, za);
        List<AxisAlignedBB> aABBS = this.world.getBoundingBoxes(box);
        return aABBS.size() <= 0;
    }

    /**
     * Applies physics to the motion of the entity (gravity and collision checks)
     * and applies the motion to the entity by altering it's position accordingly
     *
     * @param motionX motionX of the entity
     * @param motionY motionY of the entity
     * @param motionZ motionZ of the entity
     */
    public void moveEntity(float motionX, float motionY, float motionZ) {
        this.setPrevDistanceWalkedModified(this.getDistanceWalkedModified());
        float prevX = posX;
        float prevY = posY;
        float prevZ = posZ;
        float xNew = motionX;
        float yNew = motionY;
        float zNew = motionZ;

        // SNEAKING (SAFEWALK)

        boolean safeWalk = this.isOnGround() && this instanceof PlayerBase && ((PlayerBase) this).isSneaking();

        if (safeWalk) {
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

        //PHYSICS
        {
            List<AxisAlignedBB> aABBS = this.world.getBoundingBoxes(this.getBoundingBox().expand(motionX, motionY, motionZ));
            int i = 0;

            while (i < aABBS.size()) {
                motionY = aABBS.get(i).clipYCollide(this.getBoundingBox(), motionY);
                ++i;
            }
            this.getBoundingBox().move(0.0f, motionY, 0.0f);

            i = 0;
            while (i < aABBS.size()) {
                motionX = aABBS.get(i).clipXCollide(this.getBoundingBox(), motionX);
                ++i;
            }
            this.getBoundingBox().move(motionX, 0.0f, 0.0f);

            i = 0;
            while (i < aABBS.size()) {
                motionZ = aABBS.get(i).clipZCollide(this.getBoundingBox(), motionZ);
                ++i;
            }

        }
        this.getBoundingBox().move(0.0f, 0.0f, motionZ);

        this.setCollidedHorizontally(xNew != motionX || zNew != motionZ);
        this.setOnGround(yNew != motionY && yNew < 0.0f);
        if (xNew != motionX) {
            this.motionX = 0.0f;
        }
        if (yNew != motionY) {
            this.motionY = 0.0f;
        }
        if (zNew != motionZ) {
            this.motionZ = 0.0f;
        }
        this.posX = (this.getBoundingBox().x0 + this.getBoundingBox().x1) / 2.0f;
        this.posY = this.getBoundingBox().y0 + this.heightOffset;
        this.posZ = (this.getBoundingBox().z0 + this.getBoundingBox().z1) / 2.0f;
//        if (onGround) {
        double xDif = abs(this.posX) - abs(prevX);
        double yDif = abs(this.posY) - abs(prevY);
        double zDif = abs(this.posZ) - abs(prevZ);

        if ((isOnGround() || sqrt(xDif * xDif + zDif * zDif) > 0.02D) && this.canTriggerWalking() && !safeWalk) {// TODO: RIDING CHECK


            this.setDistanceWalkedModified((float) ((double) this.getDistanceWalkedModified() + Math.sqrt(xDif * xDif + zDif * zDif) * 0.6D));
            this.setDistanceWalkedOnStepModified((float) ((double) this.getDistanceWalkedOnStepModified() + Math.sqrt(xDif * xDif + yDif * yDif + zDif * zDif) * 0.6D));

            BlockPos standingOn = getPosStandingOn();
            if (this.getDistanceWalkedOnStepModified() > (float) this.nextStepDistance && world.getBlockState(standingOn) != null) {
                this.nextStepDistance = (int) this.getDistanceWalkedOnStepModified() + 1;

                //TODO: WHEN FLUIDS ARE IMPLEMENTED

//                    if (this.isInWater()) {
//                        float f = (float) (Math.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F);
//
//                        if (f > 1.0F) {
//                            f = 1.0F;
//                        }
//
//                        this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
//                    }

                this.playStepSound(standingOn);
            }
        }
//        }
    }

    @NotNull
    private BlockPos getPosStandingOn() {
        int x = (int) Math.floor(this.posX);
        int y = (int) Math.floor(this.posY - 0.20000000298023224D);
        int z = (int) Math.floor(this.posZ);
        return new BlockPos(x, y, z);
    }

    protected void playStepSound(@NotNull BlockPos standingOn) {
    }

    private boolean canTriggerWalking() {
        return true;
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
     * Updates the entities motion components
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
     * Sets the rotation yaw and pitch of the entity
     *
     * @param yaw   the new yaw
     * @param pitch the new pitch
     */
    public void setRotation(float yaw, float pitch) {
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
    }

    /**
     * @return true if the entity is swimming in a fluid
     */
    protected boolean isInFluid() {
        //TODO: IMPLEMENT AFTER IMPLEMENTING FLUID SYSTEM
        return false;
    }

    /**
     * @param chunk the given chunk
     * @return the state if the entity is in the given chunk.
     */
    public boolean isInChunk(@NotNull ChunkBase chunk) {
        return chunk.contains(posX, posZ);
    }

    @Nullable
    public WorldBase getWorld() {
        return world;
    }

    public void setWorld(@Nullable WorldBase world) {
        this.world = world;
    }

    /**
     * Called to check if the entity allows the unload of a chunk it has previously loaded
     *
     * @param chunk the chunk previously loaded by the player that is now getting unloaded
     */
    public boolean allowChunkUnload(@NotNull ChunkBase chunk) {
        return true;
    }

    /**
     * Bounding box / Hitbox of the entity.
     */
    @NotNull
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

    @NotNull
    @SideOnly(side = Side.CLIENT)
    public EntityRenderPosition getEntityRenderPosition() {
        return entityRenderPosition;
    }

    public void onSpawned(@NotNull ChunkBase chunk) {
        this.world.getChunkLoader().trackEntitySpawn(this, chunk);
    }

    /**
     * State whether the entity is currently changing chunks. Set on random thread, reset from random chunks.
     */
    public boolean isChangingChunks() {
        return newChunk != null;
    }

    /**
     * Sets the entity into the state of chunk changing
     *
     * @param newChunk the chunk the entity is being transferred to, or null, to set it into the state of not being transferred to any chunk
     */
    public void setChangingToChunk(@Nullable ChunkBase newChunk) {
        this.newChunk = newChunk;
    }

    @Nullable
    public ChunkBase getNewChunk() {
        return newChunk;
    }

    /**
     * The chunk tick time stamp when to start ticking the entity again. -1 if not set
     */
    public long getTickJoinMark() {
        return tickJoinMark;
    }

    public void joinTickAt(long tickJoinMark) {
        this.tickJoinMark = tickJoinMark;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * State if the entity is currently on ground. Updated on tick.
     */
    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    /**
     * State if the entity is collided with a block on the horizontal axis.
     */
    public boolean isCollidedHorizontally() {
        return collidedHorizontally;
    }

    public void setCollidedHorizontally(boolean collidedHorizontally) {
        this.collidedHorizontally = collidedHorizontally;
    }

    /**
     * State if the entity is dead. (Entity will be removed of the world if true in the next tick)
     */
    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public float getDistanceWalkedOnStepModified() {
        return distanceWalkedOnStepModified;
    }

    public void setDistanceWalkedOnStepModified(float distanceWalkedOnStepModified) {
        this.distanceWalkedOnStepModified = distanceWalkedOnStepModified;
    }

    /**
     * The distance walked multiplied by 0.6
     */
    public float getDistanceWalkedModified() {
        return distanceWalkedModified;
    }

    public void setDistanceWalkedModified(float distanceWalkedModified) {
        this.distanceWalkedModified = distanceWalkedModified;
    }

    /**
     * The previous ticks distance walked multiplied by 0.6
     */
    public float getPrevDistanceWalkedModified() {
        return prevDistanceWalkedModified;
    }

    public void setPrevDistanceWalkedModified(float prevDistanceWalkedModified) {
        this.prevDistanceWalkedModified = prevDistanceWalkedModified;
    }

    /**
     * Object representing the render position of the entity by combining last and current position in one object
     */
    public static class EntityRenderPosition {

        public float lastPosX, lastPosY, lastPosZ;
        public float posX, posY, posZ;

        public void set(float lastPosX, float lastPosY, float lastPosZ, float posX, float posY, float posZ) {
            this.lastPosX = lastPosX;
            this.lastPosY = lastPosY;
            this.lastPosZ = lastPosZ;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
    }

    public void setCurrentChunk(@NotNull ChunkBase currentChunk) {
        this.currentChunk = currentChunk;
    }

    public ChunkBase getCurrentChunk() {
        return currentChunk;
    }
}
