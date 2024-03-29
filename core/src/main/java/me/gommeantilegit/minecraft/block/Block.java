package me.gommeantilegit.minecraft.block;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import me.gommeantilegit.minecraft.block.material.Material;
import me.gommeantilegit.minecraft.block.state.BlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.property.BlockStateProperty;
import me.gommeantilegit.minecraft.block.state.property.BlockStatePropertyMap;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.world.WorldBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Block {

    /**
     * A unique identifier of the block
     */
    private final int id;

    /**
     * Universal bounding box min values
     */
    public float x0;
    public float y0;
    public float z0;

    /**
     * Universal bounding box max values
     */
    public float x1;
    public float y1;
    public float z1;

    /**
     * Indicates how many hits it takes to break a block.
     */
    protected float blockHardness;

    /**
     * Indicates how much this block can resist explosions
     */
    protected float blockResistance;

    private boolean transparent = false;

    /**
     * The maximum amount of blockStates which can be stored in one stack.
     */
    private final int maxItemCount = 64;

    /**
     * The unlocalized block string.
     * The unlocalizedName is translated in the language files the following:
     * tile."blockunlocalizedname".unlocalizedName=Translation
     */
    @NotNull
    private final String unlocalizedName;

    /**
     * State if the block can be collided with
     */
    private boolean collidable = true;

    /**
     * Slipperiness of block when walked on
     */
    private float slipperiness;

    /**
     * Block material instance
     */
    @NotNull
    private final Material blockMaterial;

    /**
     * The unlocalizedName of the block's parent sound type
     */
    private String soundType;

    /**
     * Stores all block state properties the block has
     */
    @NotNull
    private final List<BlockStateProperty<?>> properties = new ArrayList<>();

    /**
     * List of possible block states
     */
    @NotNull
    private final List<IBlockState> blockStates;

    /**
     * The initial version that the block appeared in
     */
    private final int initialVersion;

    /**
     * @param firstPaletteVersion  the first block state palette version that the block appeared in
     * @param id the id of the block
     * @param unlocalizedName the unlocalized translation string of the block
     * @param blockMaterial   the material of the block
     */
    public Block(int firstPaletteVersion, int id, @NotNull String unlocalizedName, @NotNull Material blockMaterial) {
        this.id = id;
        this.initialVersion = firstPaletteVersion;
        this.unlocalizedName = unlocalizedName;
        this.slipperiness = 0.6f;
        this.blockMaterial = blockMaterial;
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        this.onRegisterProperties();
        this.blockStates = createBlockStates();
    }

    @NotNull
    private List<IBlockState> createBlockStates() {
        List<IBlockState> blockStates = new ArrayList<>();
        for (BlockStateProperty<?> property : this.properties) {
            blockStates.addAll(createBlockStates(property));
        }
        if (this.properties.isEmpty()) {
            blockStates.add(newBlockState());
        }
        return blockStates;
    }

    @NotNull
    private <T> List<IBlockState> createBlockStates(@NotNull BlockStateProperty<T> property) {
        List<IBlockState> blockStates = new ArrayList<>();
        for (T allowedValue : property.getAllowedValues()) {
            BlockState state = newBlockState();
            state.setValue(property, allowedValue);
            blockStates.add(state);
        }
        return blockStates;
    }

    @NotNull
    private BlockState newBlockState() {
        return new BlockState(this) {

            @Override
            public void initProperties(@NotNull BlockStatePropertyMap.Builder builder) {
                for (BlockStateProperty<?> property : properties) {
                    addProperty(builder, property);
                }
            }

//            @Override
//            public void serialize(@NotNull BitByteBuffer buffer) {
//                super.serialize(buffer);
//                for (BlockStateProperty<?> property : properties) {
//                    serializeProperty(buffer, property);
//                }
//            }
//
//            @NotNull
//            @Override
//            public BlockState deserialize(@NotNull BitByteBuffer buffer, @NotNull Blocks blocks) throws DeserializationException {
//                BlockState blockState = super.deserialize(buffer, blocks);
//                for (BlockStateProperty<?> property : properties) {
//                    deserializeProperty(blockState, buffer, property);
//                }
//                return blockState;
//            }
//
//            private <T> void deserializeProperty(@NotNull BlockState blockState, @NotNull BitByteBuffer buffer, @NotNull BlockStateProperty<T> property) {
//                blockState.setValue(property, property.deserialize(buffer));
//            }
//
//            private <T> void serializeProperty(@NotNull BitByteBuffer buffer, @NotNull BlockStateProperty<T> property) {
//                property.serialize(buffer, getPropertyValue(property));
//            }

            private <T> void addProperty(@NotNull BlockStatePropertyMap.Builder builder, @NotNull BlockStateProperty<T> property) {
                builder.withProperty(property, property.getDefaultValue());
            }
        };
    }

    /**
     * Called to register all the block state properties of the block
     */
    protected void onRegisterProperties() {
    }


    /**
     * @return a new default block state fitting for the blocks default state in properties
     */
    @NotNull
    public IBlockState getDefaultBlockState() {
        return this.blockStates.get(0);
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
     * Sets the sound type string of the block to the specified value
     *
     * @param soundType the unlocalizedName of the sound type that the block has
     * @return self instance for the purpose of method chaining
     */
    @NotNull
    public Block setSoundType(String soundType) {
        this.soundType = soundType;
        return this;
    }

    /**
     * @see #transparent
     */
    public void setTransparent() {
        setTransparent(true);
    }

    /**
     * Called on block collision
     *
     * @param entity        the entity which collided
     * @param axisAlignedBB the boundingBox of the block it collided with
     */
    public void onEntityCollide(@NotNull Entity entity, @NotNull AxisAlignedBB axisAlignedBB) {
    }

    /**
     * Sets how many hits it takes to break a block.
     */
    @NotNull
    protected Block setHardness(float hardness) {
        this.blockHardness = hardness;
        if (this.blockResistance < hardness * 5)
            this.blockResistance = hardness * 5;
        return this;
    }

    /**
     * Sets the the blockStates resistance to explosions. Returns the object for convenience in constructing.
     */
    @NotNull
    protected Block setResistance(float resistance) {
        this.blockResistance = resistance * 3f;
        return this;
    }

    @NotNull
    protected Block setCollidable(boolean collidable) {
        this.collidable = collidable;
        return this;
    }

    /**
     * @param world      x coordinate
     * @param blockPos   y coordinate
     * @param blockState z coordinate
     * @return the bounding box of the block at the specified coordinates.
     */
    @Nullable
    public AxisAlignedBB getBoundingBox(@NotNull WorldBase world, @NotNull BlockPos blockPos, @NotNull IBlockState blockState) {
        if (this.collidable)
            return new AxisAlignedBB(blockPos.getX() + this.x0, blockPos.getY() + this.y0, blockPos.getZ() + this.z0, blockPos.getX() + this.x1, blockPos.getY() + this.y1, blockPos.getZ() + this.z1);
        else return null;
    }

    /**
     * Get the hardness of this Block relative to the ability of the given player
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
            // Declaring the vector copies in clearly defined scope to help gc
            Vector3 startCopy = start.cpy();
            Vector3 directionCopy = direction.cpy();

            Vector3 vec = startCopy.add(directionCopy.scl(range, range, range));
            return new RayTracer.RayTraceResult(vec, new BlockPos(vec), RayTracer.RayTraceResult.EnumResultType.MISS, null);
        }
    }

    public float getSlipperiness() {
        return slipperiness;
    }

    @NotNull
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public String getSoundType() {
        return soundType;
    }

    public int getInitialVersion() {
        return initialVersion;
    }

    private boolean isCollidable() {
        return collidable;
    }

    protected void registerProperty(@NotNull BlockStateProperty<?> property) {
        this.properties.add(property);
    }

    @NotNull
    public Collection<? extends IBlockState> getPossibleBlockStates() {
        return this.blockStates;
    }

    public int getId() {
        return id;
    }

    /**
     * State if the block's texture is transparent.
     */
    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }
}
