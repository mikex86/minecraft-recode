package me.gommeantilegit.minecraft.raytrace;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.lang.Math.cos;
import static java.lang.Math.toRadians;
import static java.lang.StrictMath.sin;

public class RayTracer implements IRayTracer {

    public static final float RAY_TRACE_RANGE = 4.5f;

    /**
     * The parent player to be analyzed for moving object position calculation.
     */
    @NotNull
    private final PlayerBase player;

    /**
     * Ray-trace result
     */
    @NotNull
    private RayTraceResult rayTraceResult = new RayTraceResult(new Vector3(), new BlockPos(0, 0, 0), RayTraceResult.EnumResultType.MISS, EnumFacing.NORTH);

    /**
     * Minecraft instance
     */
    @NotNull
    private final AbstractMinecraft mc;

    /**
     * @param player sets {@link #player}
     * @param mc     sets {@link #mc}
     */
    public RayTracer(@NotNull PlayerBase player, @NotNull AbstractMinecraft mc) {
        this.player = player;
        this.mc = mc;
    }

    /**
     * Called to update the utility.
     */
    public void update() {
        this.rayTraceResult = getMouseOver(mc.getTimer().partialTicks);
        this.rayTraceResult.valid = true;
    }

    /**
     * Temp vector for the return value of {@link #getPositionEyes(float)}
     */
    @NotNull
    private final Vector3 positionEyes = new Vector3();

    /**
     * Getting position vector of {@link #player}'s eyes.
     * ATTENTION: RETURN INSTANCE IS A TEMP VARIABLE. Only use one call to this method, do not the return value, only read till the next invocation of this method.
     *
     * @param partialTicks timer partial ticks
     * @return the position vector of the {@link #player}'s eyes.
     */
    @NotNull
    public Vector3 getPositionEyes(float partialTicks) {
        if (partialTicks == 1.0F) {
            return this.positionEyes.set(this.player.posX, this.player.posY + PlayerBase.DEFAULT_EYE_HEIGHT, this.player.posZ);
        } else {
            float xInterpolated = this.player.lastPosX + (this.player.posX - this.player.lastPosX) * partialTicks;
            float yInterpolated = this.player.lastPosY + (this.player.posY - this.player.lastPosY) * partialTicks + PlayerBase.DEFAULT_EYE_HEIGHT;
            float zInterpolated = this.player.lastPosZ + (this.player.posZ - this.player.lastPosZ) * partialTicks;
            return this.positionEyes.set(xInterpolated, yInterpolated, zInterpolated);
        }
    }

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    @NotNull
    public RayTraceResult getMouseOver(float partialTicks) {
        Vector3 positionEyes = this.getPositionEyes(partialTicks);
        Vector3 directionVector = getLookDirection();
        return this.player.getWorld().rayTraceBlocks(positionEyes, directionVector, RAY_TRACE_RANGE);
    }

    /**
     * @return the direction vector that the player is facing in
     */
    @NotNull
    public Vector3 getLookDirection() {
        return this.getVectorForRotation(this.player.rotationPitch, this.player.rotationYaw);
    }

    /**
     * Temp vector for the return value of {@link #getVectorForRotation(float, float)}
     */
    @NotNull
    private final Vector3 rotationVector = new Vector3();

    /**
     * ATTENTION: RETURN INSTANCE IS A TEMP VARIABLE. Only use one call to this method, do not the return value, only read till the next invocation of this method.
     * @return 3D Vector using the pitch and yaw of the entities rotation. (Direction vector (x, y, z)).
     */
    @NotNull
    private Vector3 getVectorForRotation(float pitch, float yaw) {
        double xyl = cos(toRadians(pitch));
        double y = -sin(toRadians(pitch));
        double x = -cos(toRadians(yaw - 90)) * xyl;
        double z = sin(toRadians(yaw - 90)) * xyl;
        return this.rotationVector.set((float) x, (float) y, (float) z);
    }

    @NotNull
    public RayTraceResult getRayTraceResult() {
        return Objects.requireNonNull(rayTraceResult, "Raytrace result not initialized!");
    }
}
