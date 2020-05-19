package me.gommeantilegit.minecraft.block;

import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.access.IReadableBlockStateAccess;
import me.gommeantilegit.minecraft.block.render.BlockRenderer;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.block.state.storage.BlockStateStorage;
import me.gommeantilegit.minecraft.world.chunk.builder.OptimizedMeshBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class BlockTypeRenderer {

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
     * The parent block renderer for cube rendering
     */
    @NotNull
    private final BlockRenderer renderer;

    /**
     * The block type instance that this object is capable of rendering
     */
    @NotNull
    protected final Block block;

    public BlockTypeRenderer(@NotNull Block block, @NotNull ClientMinecraft mc, @NotNull String... textureResources) {
        this.renderer = new BlockRenderer(block, mc, this);
        this.textureResources = textureResources;
        this.textureUVs = new Vector2[textureResources.length];
        int i = 0;
        for (String res : textureResources) {
            Vector2 pos = mc.textureManager.blockTextureMap.addTexture("textures/blocks/" + res + ".png");
            textureUVs[i++] = pos;
        }
        this.renderer.setTextureUVs(textureUVs);
        this.renderer.setShape(block.x0, block.y0, block.z0, block.x1, block.y1, block.z1);
        this.block = block;
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
    public void renderFace(@NotNull OptimizedMeshBuilder builder, int x, int y, int z, int face) {
        renderFace(builder, x, y, z, getUV(face), face);
    }

    /**
     * Renders the block at the specified coordinates.
     *
     * @param builder    the MeshBuilder building the chunk's mesh.
     * @param x          the x coordinate of the block
     * @param y          the y coordinate of the block
     * @param z          the z coordinate of the block
     * @param blockStateAccess    provides information about neighboring block positions to make meshing decisions or null if every face should just be rendered without checking the neighboring blockStates to determine if a face of the block should be rendered.
     * @param blockState the block state
     * @return true, if it actually rendered a face depending on the neighboring blocks
     */
    public boolean render(@NotNull OptimizedMeshBuilder builder, int x, int y, int z, @Nullable IReadableBlockStateAccess blockStateAccess, @NotNull IBlockState blockState) {
        boolean renderedFace = false;
        if (blockStateAccess == null || shouldRenderFace(blockStateAccess, x, y, z, 0)) {
            renderFace(builder, x, y, z, 0);
            renderedFace = true;
        }
        if (blockStateAccess == null || shouldRenderFace(blockStateAccess, x, y, z, 1)) {
            renderFace(builder, x, y, z, 1);
            renderedFace = true;
        }
        if (blockStateAccess == null || shouldRenderFace(blockStateAccess, x, y, z, 2)) {
            renderFace(builder, x, y, z, 2);
            renderedFace = true;
        }
        if (blockStateAccess == null || shouldRenderFace(blockStateAccess, x, y, z, 3)) {
            renderFace(builder, x, y, z, 3);
            renderedFace = true;
        }
        if (blockStateAccess == null || shouldRenderFace(blockStateAccess, x, y, z, 4)) {
            renderFace(builder, x, y, z, 4);
            renderedFace = true;
        }
        if (blockStateAccess == null || shouldRenderFace(blockStateAccess, x, y, z, 5)) {
            renderFace(builder, x, y, z, 5);
            renderedFace = true;
        }
        return renderedFace;
    }


    /**
     * Renders the specified face into the mesh-builder
     *
     * @param builder the builder for building the chunk's mesh
     * @param x       the x coordinate of the block.
     * @param y       the y coordinate of the block.
     * @param z       the z coordinate of the block.
     * @param face    current face rendered.
     * @param uv      texture uv coordinates
     */
    public void renderFace(@NotNull OptimizedMeshBuilder builder, int x, int y, int z, @NotNull Vector2 uv, int face) {
        this.renderer.renderFace(builder, x, y, z, uv, face);
    }

    /**
     * @param stateProvider the block state provider
     * @param x             x coordinate
     * @param y             y coordinate
     * @param z             z coordinate
     * @param face          the face to be checked.
     * @return if the given face should be rendered at the specified coordinates considering the blockStates neighbors.
     * The face should only be rendered if it's visible.
     */
    private boolean shouldRenderFace(@NotNull IReadableBlockStateAccess stateProvider, int x, int y, int z, int face) {
        switch (face) {
            case 0:
                return canSeeThrough(stateProvider, x, y - 1, z);
            case 1:
                return canSeeThrough(stateProvider, x, y + 1, z);
            case 2:
                return canSeeThrough(stateProvider, x, y, z - 1);
            case 3:
                return canSeeThrough(stateProvider, x, y, z + 1);
            case 4:
                return canSeeThrough(stateProvider, x - 1, y, z);
            case 5:
                return canSeeThrough(stateProvider, x + 1, y, z);
            default:
                return false;
        }
    }

    private boolean canSeeThrough(@NotNull IReadableBlockStateAccess access, int x, int y, int z) {
        IBlockState blockState = access.getBlockState(x, y, z);
        return this.canSeeThrough(blockState);
    }


    /**
     * @param blockState the given block state or null for air
     * @return true if the block is has transparency or is air.
     */
    public final boolean canSeeThrough(@Nullable IBlockState blockState) {
        return blockState == null || blockState.getBlock().isTransparent();
    }

    @NotNull
    public String[] getTextureResources() {
        return textureResources;
    }

    @NotNull
    public Vector2[] getTextureUVs() {
        return textureUVs;
    }

    @NotNull
    public Vector2 getUV(int face) {
        return this.textureUVs[0];
    }

}
