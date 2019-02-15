package me.gommeantilegit.minecraft.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import org.jetbrains.annotations.NotNull;

import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.block.texturemap.BlockTextureMap;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("Duplicates")
public class Block {

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
     * Block name
     */
    @NotNull
    private final String name;

    /**
     * All block texture files that the block uses.
     */
    @NotNull
    private final String[] textureResources;

    /**
     * textureUVs[x] is the uv position of the image {@link #textureResources}[x] in the final texture map.
     */
    @NotNull
    protected final Vector2[] textureUVs;

    /**
     * ID of Block
     */
    private final int id;

    /**
     * State if the block can be collided with
     */
    private final boolean collidable;

    /**
     * Slipperiness of block when walked on
     */
    private float slipperiness;

    public Block(@NotNull String name, int id, @NotNull String[] textureResources, boolean collidable) {
        this.name = name;
        this.id = id;
        this.textureResources = textureResources;
        this.textureUVs = new Vector2[textureResources.length];
        this.collidable = collidable;
        this.slipperiness = 0.6f;
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        Blocks.registerBlock(this);
        int i = 0;
        for (String res : textureResources) {
            Vector2 pos = Minecraft.mc.textureManager.blockTextureMap.addTexuture("textures/blocks/" + res + ".png");
            textureUVs[i++] = pos;
        }
    }

    /**
     * Default facing EnumFacing.UP
     *
     * @return the default block state
     */
    @NotNull
    public IBlockState getDefaultBlockState() {
        return new BlockState(this, EnumFacing.UP);
    }

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
    public Block setTransparent() {
        transparent = true;
        return this;
    }

    /**
     * Renders the block at the specified coordinates.
     *
     * @param builder        the MeshBuilder building the chunk's mesh.
     * @param x              the x coordinate of the block.
     * @param y              the y coordinate of the block.
     * @param z              the z coordinate of the block.
     * @param world          the world of the block.
     * @param blockState     the block state
     * @param renderAllFaces state if every face should just be rendered without checking the neighboring blockStates to determine if a face of the block should be rendered.
     */
    public void render(MeshBuilder builder, int x, int y, int z, @Nullable World world, @NotNull IBlockState blockState, boolean renderAllFaces) {
        if (!renderAllFaces && world == null) {
            throw new IllegalStateException("Error while rendering block to MeshBuilder: " +
                    "Specified world parameter is equal to null but state of parameter renderAllFaces is set to false." +
                    " Parameter world is only nullable, if renderAllFaces is set to true.");
        }
        //TODO FACING ROTATION
        if (renderAllFaces || shouldRenderFace(world, x, y, z, 0))
            renderFace(builder, x, y, z, 0);
        if (renderAllFaces || shouldRenderFace(world, x, y, z, 1))
            renderFace(builder, x, y, z, 1);
        if (renderAllFaces || shouldRenderFace(world, x, y, z, 2))
            renderFace(builder, x, y, z, 2);
        if (renderAllFaces || shouldRenderFace(world, x, y, z, 3))
            renderFace(builder, x, y, z, 3);
        if (renderAllFaces || shouldRenderFace(world, x, y, z, 4))
            renderFace(builder, x, y, z, 4);
        if (renderAllFaces || shouldRenderFace(world, x, y, z, 5))
            renderFace(builder, x, y, z, 5);
    }

    /**
     * @param world the world of the block.
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @param face  the face to be checked.
     * @return if the given face should be rendered at the specified coordinates considering the blockStates neighbors.
     * The face should only be rendered if it's visible.
     */
    private boolean shouldRenderFace(World world, int x, int y, int z, int face) {
        switch (face) {
            case 0:
                return world.canSeeThrough(world.getBlockID(x, y - 1, z));
            case 1:
                return world.canSeeThrough(world.getBlockID(x, y + 1, z));
            case 2:
                return world.canSeeThrough(world.getBlockID(x, y, z - 1));
            case 3:
                return world.canSeeThrough(world.getBlockID(x, y, z + 1));
            case 4:
                return world.canSeeThrough(world.getBlockID(x - 1, y, z));
            case 5:
                return world.canSeeThrough(world.getBlockID(x + 1, y, z));
            default:
                return false;
        }
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
    public Block setHardness(float hardness) {
        this.blockHardness = hardness;
        if (this.blockResistance < hardness * 5)
            this.blockResistance = hardness * 5;
        return this;
    }

    /**
     * Sets the the blockStates resistance to explosions. Returns the object for convenience in constructing.
     */
    public Block setResistance(float resistance) {
        this.blockResistance = resistance * 3f;
        return this;
    }

    /**
     * @param world      x coordinate
     * @param blockPos   y coordinate
     * @param blockState z coordinate
     * @return the bounding box of the block at the specified coordinates.
     */
    @Nullable
    public AxisAlignedBB getBoundingBox(@NotNull World world, @NotNull BlockPos blockPos, @NotNull IBlockState blockState) {
        return new AxisAlignedBB(blockPos.getX() + this.x0, blockPos.getY() + this.y0, blockPos.getZ() + this.z0, blockPos.getX() + this.x1, blockPos.getY() + this.y1, blockPos.getZ() + this.z1);
    }

    /**
     * Get the hardness of this Block relative to the ability of the given player
     */
    public float getPlayerRelativeBlockHardness(@NotNull Player player, @NotNull World worldIn, @NotNull BlockPos pos) {
        return blockHardness < 0f ? 0f : (!player.canHarvestBlock(this) ? player.getToolDigEfficiency(this) / blockHardness / 100f : player.getToolDigEfficiency(this) / blockHardness / 30f);
    }

    /**
     * @param state       the block state taken into account of the check whether or not the block is collidable in it's current block state
     * @return if the block can be collided with considering paramters state and hitIfLiquid
     */
    public boolean canCollideCheck(@NotNull IBlockState state) {
        return this.isCollidable();
    }

    /**
     * @return true if a vector is within the Y and Z bounds of the block.
     */
    private boolean isVecInsideYZBounds(@NotNull Vector3 point) {
        return point.y >= this.y0 && point.y <= this.y1 && point.z >= this.z0 && point.z <= this.z1;
    }

    /**
     * @return true if a vector is within the X and Z bounds of the block.
     */
    private boolean isVecInsideXZBounds(@NotNull Vector3 point) {
        return point.x >= this.x0 && point.x <= this.x1 && point.z >= this.z0 && point.z <= this.z1;
    }

    /**
     * @return true if  a vector is within the X and Y bounds of the block.
     */
    private boolean isVecInsideXYBounds(@NotNull Vector3 point) {
        return point.x >= this.x0 && point.x <= this.x1 && point.y >= this.y0 && point.y <= this.y1;
    }

    /**
     * Renders the specified face into the meshbuilder
     *
     * @param builder the builder for building the chunk's mesh
     * @param x       the x coordinate of the block.
     * @param y       the y coordinate of the block.
     * @param z       the z coordinate of the block.
     * @param face    current face rendered.
     */
    public void renderFace(MeshBuilder builder, int x, int y, int z, int face) {
        renderFace(builder, x, y, z, getUV(face), face);
    }

    public void renderFace(MeshBuilder builder, int x, int y, int z, Vector2 uv, int face) {
        float u0 = uv.x;
        float u1 = uv.x + 16;
        float v0 = uv.y;
        float v1 = uv.y + 16;

        BlockTextureMap map = Minecraft.mc.textureManager.blockTextureMap;
        u0 /= map.getWidth();
        u1 /= map.getWidth();
        v0 /= map.getHeight();
        v1 /= map.getHeight();

        float x0 = (float) x + this.x0;
        float x1 = (float) x + this.x1;
        float y0 = (float) y + this.y0;
        float y1 = (float) y + this.y1;
        float z0 = (float) z + this.z0;
        float z1 = (float) z + this.z1;

        switch (face) {
            case 0: {
                rect(builder,
                        x1, y0, z1,
                        u1, v1,
                        x1, y0, z0,
                        u1, v0,
                        x0, y0, z0,
                        u0, v0,
                        x0, y0, z1,
                        u0, v1,
                        0, -1, 0);
            }
            return;
            case 1: {
                rect(builder,
                        x0, y1, z1,
                        u0, v1,
                        x0, y1, z0,
                        u0, v0,
                        x1, y1, z0,
                        u1, v0,
                        x1, y1, z1,
                        u1, v1,
                        0, 1, 0);
                return;
            }
            case 2: {
                rect(builder,
                        x0, y0, z0,
                        u1, v1,
                        x1, y0, z0,
                        u0, v1,
                        x1, y1, z0,
                        u0, v0,
                        x0, y1, z0,
                        u1, v0,
                        0, 0, -1);
                return;
            }
            case 3: {
                rect(builder,
                        x1, y1, z1,
                        u1, v0,
                        x1, y0, z1,
                        u1, v1,
                        x0, y0, z1,
                        u0, v1,
                        x0, y1, z1,
                        u0, v0,
                        0, 0, 1);
                return;

            }
            case 4: {
                rect(builder,
                        x0, y0, z1,
                        u1, v1,
                        x0, y0, z0,
                        u0, v1,
                        x0, y1, z0,
                        u0, v0,
                        x0, y1, z1,
                        u1, v0,
                        -1, 0, 0);
                return;
            }
            case 5: {
                rect(builder,
                        x1, y1, z1,
                        u0, v0,

                        x1, y1, z0,
                        u1, v0,

                        x1, y0, z0,
                        u1, v1,

                        x1, y0, z1,
                        u0, v1,
                        1, 0, 0);
            }
        }

    }


    protected void rect(MeshBuilder builder,
                        float x00, float y00, float z00,
                        float u00, float v00,
                        float x10, float y10, float z10,
                        float u10, float v10,
                        float x11, float y11, float z11,
                        float u11, float v11,
                        float x01, float y01, float z01,
                        float u01, float v01,
                        float normalX, float normalY, float normalZ) {

        builder.rect(
                new MeshPartBuilder.VertexInfo().setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(u00, v00),
                new MeshPartBuilder.VertexInfo().setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(u10, v10),
                new MeshPartBuilder.VertexInfo().setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(u11, v11),
                new MeshPartBuilder.VertexInfo().setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(u01, v01)
        );

    }

    protected void rect(MeshBuilder builder,
                        float x00, float y00, float z00,
                        float u00, float v00,
                        float x10, float y10, float z10,
                        float u10, float v10,
                        float x11, float y11, float z11,
                        float u11, float v11,
                        float x01, float y01, float z01,
                        float u01, float v01,
                        float normalX, float normalY, float normalZ, float r, float g, float b, float a) {

        builder.rect(
                new MeshPartBuilder.VertexInfo().setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(u00, v00).setCol(r, g, b, a),
                new MeshPartBuilder.VertexInfo().setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(u10, v10).setCol(r, g, b, a),
                new MeshPartBuilder.VertexInfo().setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(u11, v11).setCol(r, g, b, a),
                new MeshPartBuilder.VertexInfo().setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(u01, v01).setCol(r, g, b, a)
        );

    }

    public float getSlipperiness() {
        return slipperiness;
    }

    protected Vector2 getUV(int face) {
        return this.textureUVs[0];
    }

    public int getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public String[] getTextureResources() {
        return textureResources;
    }

    public Vector2[] getTextureUVs() {
        return textureUVs;
    }

    private boolean isCollidable() {
        return collidable;
    }

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    @NotNull
    public RayTracer.RayTraceResult collisionRayTrace(BlockPos pos, Vector3 start, Vector3 direction, float range) {
        Ray ray = new Ray(start, direction);
        Vector3 intersectionVector = new Vector3();
        boolean intersectionPresent = Intersector.intersectRayBounds(ray, new BoundingBox(new Vector3(x0, y0, z0).add(pos.asVector()), new Vector3(x1, y1, z1).add(pos.asVector())), intersectionVector);
        if (intersectionPresent) {
            EnumFacing facing = BlockPos.getFacing(intersectionVector, pos);
            return new RayTracer.RayTraceResult(intersectionVector, pos, RayTracer.RayTraceResult.EnumResultType.BLOCK, facing);
        } else {
            return new RayTracer.RayTraceResult(null, null, RayTracer.RayTraceResult.EnumResultType.MISS, null);
        }
    }

}
