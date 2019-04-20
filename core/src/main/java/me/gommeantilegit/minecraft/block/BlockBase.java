package me.gommeantilegit.minecraft.block;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.block.state.BlockStateBase;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <MC> Minecraft type
 * @param <BB> BlockBase type that extends this base class
 */
public abstract class BlockBase<MC extends AbstractMinecraft, BB extends BlockBase<MC, BB, BS, BLOCKS>, BS extends BlockStateBase<BB>, BLOCKS extends Blocks<BB, MC>> {

    /**
     * Universal bounding box min values
     */
    protected float x0, y0, z0;

    /**
     * Universal bounding box max values
     */
    protected float x1, y1, z1;

    /**
     * Indicates how many hits it takes to break a block.
     */
    protected float blockHardness;

    /**
     * Indicates how much this block can resist explosions
     */
    protected float blockResistance;

    /**
     * State if the block's texture is transparent.
     */
    public boolean transparent = false;

    /**
     * The maximum amount of blockStates which can be stored in one stack.
     */
    private int maxItemCount = 64;

    /**
     * BlockBase name
     */
    @NotNull
    private final String name;

    /**
     * ID of BlockBase
     */
    private final int id;

    /**
     * State if the block has enum facing (can have rotation)
     */
    private final boolean hasEnumFacing;

    /**
     * State if the block can be collided with
     */
    private final boolean collidable;

    /**
     * Slipperiness of block when walked on
     */
    private float slipperiness;

    /**
     * Minecraft instance
     */
    @NotNull
    public final MC mc;

    public BlockBase(@NotNull String name, int id, boolean hasEnumFacing, boolean collidable, @NotNull BLOCKS blocks, @NotNull MC mc) {
        this.name = name;
        this.id = id;
        this.hasEnumFacing = hasEnumFacing;
        this.collidable = collidable;
        this.slipperiness = 0.6f;
        this.mc = mc;
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        blocks.registerBlock((BB) this);
    }

    /**
     * Default facing EnumFacing.UP
     *
     * @return the default block state
     */
    @NotNull
    public abstract BS getDefaultBlockState();

    /**
     * Setting the shape of the block
     *
     * @param x0 minX
     * @param y0 minY
     * @param z0 minZ
     * @param x1 maxX
     * @param y1 maxY
     * @param z1 maxZ
     */
    protected void setShape(float x0, float y0, float z0, float x1, float y1, float z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    /**
     * @see #transparent
     */
    public BB setTransparent() {
        transparent = true;
        return (BB) this;
    }

    /**
     * Called on block collision
     *
     * @param entity        the entity which collided
     * @param axisAlignedBB the boundingBox of the block it collided with
     */
    public void onEntityCollide(Entity entity, AxisAlignedBB axisAlignedBB) {
    }

    /**
     * Sets how many hits it takes to break a block.
     */
    public BB setHardness(float hardness) {
        this.blockHardness = hardness;
        if (this.blockResistance < hardness * 5)
            this.blockResistance = hardness * 5;
        return (BB) this;
    }

    /**
     * Sets the the blockStates resistance to explosions. Returns the object for convenience in constructing.
     */
    public BB setResistance(float resistance) {
        this.blockResistance = resistance * 3f;
        return (BB) this;
    }

    /**
     * @param world      x coordinate
     * @param blockPos   y coordinate
     * @param blockState z coordinate
     * @return the bounding box of the block at the specified coordinates.
     */
    @Nullable
    public AxisAlignedBB getBoundingBox(@NotNull WorldBase world, @NotNull BlockPos blockPos, @NotNull IBlockState blockState) {
        return new AxisAlignedBB(blockPos.getX() + this.x0, blockPos.getY() + this.y0, blockPos.getZ() + this.z0, blockPos.getX() + this.x1, blockPos.getY() + this.y1, blockPos.getZ() + this.z1);
    }

    /**
     * Get the hardness of this BlockBase relative to the ability of the given player
     */
    public float getPlayerRelativeBlockHardness(@NotNull PlayerBase player, @NotNull WorldBase worldIn, @NotNull BlockPos pos) {
        return blockHardness < 0f ? 0f : (!player.canHarvestBlock(this) ? player.getToolDigEfficiency(this) / blockHardness / 100f : player.getToolDigEfficiency(this) / blockHardness / 30f);
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    @NotNull
    public RayTracer.RayTraceResult collisionRayTrace(BlockPos pos, Vector3 start, Vector3 direction, float range) {
        Ray ray = new Ray(start, direction);
        Vector3 intersectionVector = new Vector3();
        boolean intersectionPresent = Intersector.intersectRayBounds(ray, new BoundingBox(new Vector3(x0, y0, z0).add(pos.asVector()), new Vector3(x1, y1, z1).add(pos.asVector())), intersectionVector);
        if (intersectionPresent && start.dst(intersectionVector) <= range) {
            EnumFacing facing = BlockPos.getFacing(intersectionVector, pos);
            return new RayTracer.RayTraceResult(intersectionVector, pos, RayTracer.RayTraceResult.EnumResultType.BLOCK, facing);
        } else {
            Vector3 vec = start.cpy().add(direction.cpy().scl(range, range, range));
            return new RayTracer.RayTraceResult(vec, new BlockPos(vec), RayTracer.RayTraceResult.EnumResultType.MISS, null);
        }
    }

    public float getSlipperiness() {
        return slipperiness;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    private boolean isCollidable() {
        return collidable;
    }

    public boolean hasEnumFacing() {
        return hasEnumFacing;
    }

}
