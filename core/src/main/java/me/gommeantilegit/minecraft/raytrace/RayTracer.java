package me.gommeantilegit.minecraft.raytrace;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.render.BlockHighlighter;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.ws.soap.MTOM;
import java.util.ArrayList;
import java.util.Collection;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static java.lang.Math.toRadians;

public class RayTracer {

    public static final float RAY_TRACE_RANGE = 4.5f;

    /**
     * The parent player to be analyzed for moving object position calculation.
     */
    private final Player player;

    /**
     * The player's view camera
     */
    private final PerspectiveCamera camera;

    /**
     * Ray-trace result
     */
    public RayTraceResult rayTraceResult;

    /**
     * The rendering util used to highlight the block ray-traced by the players viewport.
     */
    @NotNull
    private final BlockHighlighter blockHighlighter;

    /**
     * @param player sets {@link #player}
     */
    public RayTracer(Player player) {
        this.player = player;
        this.camera = player.camera;
        this.blockHighlighter = new BlockHighlighter(player);
    }

    /**
     * Called to update the utility.
     */
    public void update() {
        rayTraceResult = getMouseOver(Minecraft.mc.timer.partialTicks);
        rayTraceResult.valid = true;
        render();
    }

    /**
     * Getting position vector of {@link #player}'s eyes.
     *
     * @param partialTicks timer partial ticks
     * @return the position vector of the {@link #player}'s eyes.
     */
    @NotNull
    public Vector3 getPositionEyes(float partialTicks) {
        if (partialTicks == 1.0F) {
            return new Vector3(this.player.posX, this.player.posY + Player.EYE_HEIGHT, this.player.posZ);
        } else {
            float xInterpolated = this.player.lastPosX + (this.player.posX - this.player.lastPosX) * partialTicks;
            float yInterpolated = this.player.lastPosY + (this.player.posY - this.player.lastPosY) * partialTicks + Player.EYE_HEIGHT;
            float zInterpolated = this.player.lastPosZ + (this.player.posZ - this.player.lastPosZ) * partialTicks;
            return new Vector3(xInterpolated, yInterpolated, zInterpolated);
        }
    }

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    @NotNull
    public RayTraceResult getMouseOver(float partialTicks) {
        Vector3 positionEyes = this.getPositionEyes(partialTicks);
        Vector3 directionVector = this.camera.direction;
        return this.player.getWorld().rayTraceBlocks(positionEyes, directionVector, RAY_TRACE_RANGE);
    }

    /**
     * @param partialTicks timer partial ticks
     * @return the direction vector the interpolated look vector
     */
    @NotNull
    public Vector3 getLook(float partialTicks) {
        if (partialTicks == 1.0F) {
            return this.getVectorForRotation(this.player.rotationPitch, this.player.rotationYaw);
        } else {
            float pitch = this.player.lastRotationPitch + (this.player.rotationPitch - this.player.lastRotationPitch) * partialTicks;
            float yaw = this.player.lastRotationYaw + (this.player.rotationYaw - this.player.lastRotationYaw) * partialTicks;
            return this.getVectorForRotation(pitch, yaw);
        }
    }

    /**
     * @return 3D Vector using the pitch and yaw of the entities rotation. (Direction vector (x, y, z))
     */
    @NotNull
    private Vector3 getVectorForRotation(float pitch, float yaw) {
        float z = (float) Math.cos(toRadians(-yaw) - (float) Math.PI);
        float x = (float) Math.sin(toRadians(-yaw) - (float) Math.PI);
        float rpo = (float) -Math.cos(toRadians(-pitch));
        float y = (float) Math.sin(toRadians(-pitch));
        return new Vector3(x * rpo, y, z * rpo);
    }

    /**
     * Highlights the ray traced block
     */
    public void render() {
        if (rayTraceResult == null) return;
        if (rayTraceResult.type == RayTraceResult.EnumResultType.BLOCK) {
            assert rayTraceResult.blockPos != null;
            this.blockHighlighter.setBlockPos(rayTraceResult.blockPos.getX(), rayTraceResult.blockPos.getY(), rayTraceResult.blockPos.getZ());
        }
        if (rayTraceResult.type != RayTraceResult.EnumResultType.MISS) {
            this.blockHighlighter.render();
        }
    }

    public static class RayTraceResult {

        /**
         * The position where the rayCast hit an object
         */
        public final Vector3 hitVec;

        /**
         * The faced block aligned position. Nullable
         */
        @Nullable
        private final BlockPos blockPos;

        /**
         * Represents the type of result. eg. if a block is faced or if its a miss. Later on an entity can be also faced at.
         */
        public enum EnumResultType {
            BLOCK, MISS, ENTITY
        }

        /**
         * The type of the result.
         *
         * @see EnumResultType
         */
        @NotNull
        public final EnumResultType type;

        /**
         * State if the moving object position is final and will not be changed this frame.
         */
        public boolean valid;

        /**
         * Facing, how the player is facing the block
         */
        @Nullable
        public final EnumFacing hitSide;

        /**
         * Entity faced. Null, if type is {@link RayTraceResult.EnumResultType#MISS} or {@link RayTraceResult.EnumResultType#BLOCK}
         */
        @Nullable
        private final Entity entity;

        /**
         * @param hitVec   sets {@link #hitVec}
         * @param blockPos sets {@link #blockPos}
         * @param type     sets {@link #type}
         */
        public RayTraceResult(@Nullable Vector3 hitVec, @Nullable BlockPos blockPos, @NotNull EnumResultType type, @Nullable EnumFacing hitSide) {
            this.hitVec = hitVec;
            this.blockPos = blockPos;
            this.type = type;
            this.hitSide = hitSide;
            this.entity = null;
        }

        public RayTraceResult(@Nullable Entity entityHitIn, @Nullable Vector3 hitVecIn) {
            this.type = EnumResultType.ENTITY;
            this.entity = entityHitIn;
            this.hitVec = hitVecIn;
            this.hitSide = null;
            this.blockPos = new BlockPos(hitVec);
        }

        @Nullable
        public BlockPos getBlockPos() {
            return blockPos;
        }

        @Nullable
        public Entity getEntity() {
            return entity;
        }

        @Nullable
        public EnumFacing getHitSide() {
            return hitSide;
        }

        @NotNull
        public EnumResultType getType() {
            return type;
        }
    }
}
