package me.gommeantilegit.minecraft.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.render.BlockRenderer;
import me.gommeantilegit.minecraft.block.state.ClientBlockState;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.gommeantilegit.minecraft.Side.CLIENT;

@SideOnly(side = CLIENT)
public class ClientBlock extends BlockBase<ClientMinecraft, ClientBlock, ClientBlockState, ClientBlocks> {

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

    private BlockRenderer renderer;

    public ClientBlock(@NotNull String name, int id, @NotNull String[] textureResources, boolean collidable, @NotNull ClientBlocks blocks, @NotNull ClientMinecraft mc, boolean hasEnumFacing) {
        super(name, id, hasEnumFacing, collidable, blocks, mc);
        this.textureResources = textureResources;
        this.textureUVs = new Vector2[textureResources.length];
        int i = 0;
        for (String res : textureResources) {
            Vector2 pos = mc.textureManager.blockTextureMap.addTexture("textures/blocks/" + res + ".png");
            textureUVs[i++] = pos;
        }
        this.renderer.setTextureUVs(textureUVs);
    }

    @NotNull
    @Override
    public ClientBlockState getDefaultBlockState() {
        return new ClientBlockState(this, EnumFacing.defaultFacing());
    }

    @Override
    protected void setShape(float x0, float y0, float z0, float x1, float y1, float z1) {
        super.setShape(x0, y0, z0, x1, y1, z1);
        if (renderer == null)
            renderer = new BlockRenderer(this);
        this.renderer.setShape(x0, y0, z0, x1, y1, z1);
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
    public void renderFace(@NotNull MeshBuilder builder, int x, int y, int z, int face) {
        renderFace(builder, x, y, z, getUV(face), face);
    }

    /**
     * Renders the block at the specified coordinates.
     *
     * @param builder        the MeshBuilder building the chunk's mesh.
     * @param renderX        the x coordinate where the block should be rendered
     * @param renderY        the y coordinate of the block.
     * @param renderZ        the z coordinate of the block.
     * @param xCheck         the x component of the block pos which neighbors are checked in blockState using the specified world instance to determine face visibility
     * @param yCheck         the y component of the block pos which neighbors are checked in blockState using the specified world instance to determine face visibility
     * @param zCheck         the z component of the block pos which neighbors are checked in blockState using the specified world instance to determine face visibility
     * @param world          the world of the block.
     * @param blockState     the block state
     * @param renderAllFaces state if every face should just be rendered without checking the neighboring blockStates to determine if a face of the block should be rendered.
     */
    public void render(MeshBuilder builder, int renderX, int renderY, int renderZ, int xCheck, int yCheck, int zCheck, @Nullable ClientWorld world, @NotNull IBlockState blockState, boolean renderAllFaces) {
        if (!renderAllFaces && world == null) {
            throw new IllegalStateException("Error while rendering block to MeshBuilder: " +
                    "Specified world parameter is equal to null but state of parameter renderAllFaces is set to false." +
                    " Parameter world is only nullable, if renderAllFaces is set to true.");
        }
        //TODO FACING ROTATION
        if (renderAllFaces || shouldRenderFace(world, xCheck, yCheck, zCheck, 0))
            renderFace(builder, renderX, renderY, renderZ, 0);
        if (renderAllFaces || shouldRenderFace(world, xCheck, yCheck, zCheck, 1))
            renderFace(builder, renderX, renderY, renderZ, 1);
        if (renderAllFaces || shouldRenderFace(world, xCheck, yCheck, zCheck, 2))
            renderFace(builder, renderX, renderY, renderZ, 2);
        if (renderAllFaces || shouldRenderFace(world, xCheck, yCheck, zCheck, 3))
            renderFace(builder, renderX, renderY, renderZ, 3);
        if (renderAllFaces || shouldRenderFace(world, xCheck, yCheck, zCheck, 4))
            renderFace(builder, renderX, renderY, renderZ, 4);
        if (renderAllFaces || shouldRenderFace(world, xCheck, yCheck, zCheck, 5))
            renderFace(builder, renderX, renderY, renderZ, 5);
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
    public void renderFace(@NotNull MeshBuilder builder, int x, int y, int z, @NotNull Vector2 uv, int face) {
        this.renderer.renderFace(builder, x, y, z, uv, face);
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
    private boolean shouldRenderFace(@NotNull ClientWorld world, int x, int y, int z, int face) {
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
