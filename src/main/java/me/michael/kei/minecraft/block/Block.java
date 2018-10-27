package me.michael.kei.minecraft.block;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector2;
import com.sun.istack.internal.NotNull;
import me.michael.kei.minecraft.block.texturemap.BlockTextureMap;
import me.michael.kei.minecraft.world.World;

public class Block {

    protected float xx0, yy0, zz0;

    protected float xx1, yy1, zz1;

    /**
     * State if the block's texture is transparent.
     */
    public boolean transparent = false;

    /**
     * The maximum amount of blocks which can be stored in one stack.
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
    private final Vector2[] textureUVs;

    /**
     * TextureMap object
     */
    @NotNull
    public static final BlockTextureMap TEXTURE_MAP = new BlockTextureMap(256);

    /**
     * ID of Block
     */
    private final int id;

    public Block(@NotNull String name, int id, @NotNull String[] textureResources) {
        this.name = name;
        this.id = id;
        this.textureResources = textureResources;
        this.textureUVs = new Vector2[textureResources.length];
        this.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        Blocks.registerBlock(this);
        int i = 0;
        for (String res : textureResources) {
            Vector2 pos = TEXTURE_MAP.addTexuture("textures/blocks/" + res + ".png");
            textureUVs[i++] = pos;
        }
    }

    protected void setShape(float x0, float y0, float z0, float x1, float y1, float z1) {
        this.xx0 = x0;
        this.yy0 = y0;
        this.zz0 = z0;
        this.xx1 = x1;
        this.yy1 = y1;
        this.zz1 = z1;
    }

    /**
     * @see #transparent
     */
    public Block setTransparent() {
        transparent = true;
        return this;
    }

    public void render(MeshBuilder builder, int x, int y, int z, World world, boolean renderAllFaces) {
        if (shouldRenderFace(world, x, y, z, 0) || renderAllFaces)
            renderFace(builder, x, y, z, 0);
        if (shouldRenderFace(world, x, y, z, 1) || renderAllFaces)
            renderFace(builder, x, y, z, 1);
        if (shouldRenderFace(world, x, y, z, 2) || renderAllFaces)
            renderFace(builder, x, y, z, 2);
        if (shouldRenderFace(world, x, y, z, 3) || renderAllFaces)
            renderFace(builder, x, y, z, 3);
        if (shouldRenderFace(world, x, y, z, 4) || renderAllFaces)
            renderFace(builder, x, y, z, 4);
        if (shouldRenderFace(world, x, y, z, 5) || renderAllFaces)
            renderFace(builder, x, y, z, 5);
    }

    private boolean shouldRenderFace(World world, int x, int y, int z, int face) {
        switch (face) {
            case 0:
                return canSeeThrough(world.getBlockID(x, y - 1, z));
            case 1:
                return canSeeThrough(world.getBlockID(x, y + 1, z));
            case 2:
                return canSeeThrough(world.getBlockID(x, y, z - 1));
            case 3:
                return canSeeThrough(world.getBlockID(x, y, z + 1));
            case 4:
                return canSeeThrough(world.getBlockID(x - 1, y, z));
            case 5:
                return canSeeThrough(world.getBlockID(x + 1, y, z));
            default:
                return false;
        }
    }

    private boolean canSeeThrough(int blockID) {
        Block block = Blocks.getBlockByID(blockID);
        return block == null || block.transparent;
    }

    public void renderFace(MeshBuilder builder, int x, int y, int z, int face) {

        Vector2 uv = getUV(face);

        float u0 = uv.x;
        float u1 = uv.x + 16;
        float v0 = uv.y;
        float v1 = uv.y + 16;

        u0 /= TEXTURE_MAP.getWidth();
        u1 /= TEXTURE_MAP.getWidth();
        v0 /= TEXTURE_MAP.getHeight();
        v1 /= TEXTURE_MAP.getHeight();

        float x0 = (float) x + this.xx0;
        float x1 = (float) x + this.xx1;
        float y0 = (float) y + this.yy0;
        float y1 = (float) y + this.yy1;
        float z0 = (float) z + this.zz0;
        float z1 = (float) z + this.zz1;

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
                        x0, y1, z1,
                        u1, v1,
                        x0, y1, z0,
                        u0, v1,
                        x0, y0, z0,
                        u0, v0,
                        x0, y0, z1,
                        u1, v0,
                        -1, 0, 0);
                return;
            }
            case 5: {
                rect(builder,
                        x1, y1, z1, u0, v0,
                        x1, y1, z0, u1, v0,
                        x1, y0, z0, u1, v1,
                        x1, y0, z1, u0, v1,
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

    protected Vector2 getUV(int face) {
        return this.textureUVs[0];
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getTextureResources() {
        return textureResources;
    }

    public Vector2[] getTextureUVs() {
        return textureUVs;
    }

}
