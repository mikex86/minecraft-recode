package me.gommeantilegit.minecraft.raytrace.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector2;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.util.renderer.BlockRenderer;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.GL_POLYGON_OFFSET_FILL;

public class BlockHighlighter {

    /**
     * Positional components of the block being highlighted
     */
    private int blockX, blockY, blockZ;

    /**
     * Block mesh
     */
    @NotNull
    private final Mesh linesMesh;

    @NotNull
    private final Mesh trianglesMesh;

    /**
     * Parent player
     */
    @NotNull
    private final Player player;

    public BlockHighlighter(@NotNull Player player) {
        this.player = player;
        //Lines mesh
        {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.setColor(1, 0, 0, 1);
            meshBuilder.begin(Minecraft.mc.vertexAttributes, GL20.GL_LINES);
            {
                float xo = 0.5f, yo = 0.5f, zo = 0.5f;
                float tolerance = 0f;
                meshBuilder.box(xo, yo, zo, 1 + tolerance, 1 + tolerance, 1 + tolerance);
            }
            this.linesMesh = meshBuilder.end();
        }
        //Triangles mesh
        {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.setColor(1, 0, 0, 1);
            meshBuilder.begin(Minecraft.mc.vertexAttributes, GL20.GL_TRIANGLES);
            {
                createBlockMesh(meshBuilder);
            }
            this.trianglesMesh = meshBuilder.end();
        }
    }

    private void createBlockMesh(@NotNull MeshBuilder meshBuilder) {
        BlockRenderer renderer = new BlockRenderer(new Vector2[]{
                new Vector2(0, 0)
        }, Minecraft.mc.textureManager.blockDestroyStages);
        renderer.render(meshBuilder, 0, 0, 0);
    }

    /**
     * Sets the value of the position vector defining the block being highlighted
     *
     * @param blockX sets {@link #blockX}
     * @param blockY sets {@link #blockY}
     * @param blockZ sets {@link #blockZ}
     */
    public void setBlockPos(int blockX, int blockY, int blockZ) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    /**
     * Renders the mesh highlighting the given block specified using {@link #setBlockPos(int, int, int)}
     * Note:
     * - Changes line-width to 4. Resets it to 1 afterwards.
     */
    public void render() {
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        Gdx.gl.glEnable(GL_POLYGON_OFFSET_FILL);
        Gdx.gl.glPolygonOffset(0, -200);
        StdShader shader = Minecraft.mc.shaderManager.stdShader;
        shader.translate(blockX, blockY, blockZ);
        {
            Gdx.gl20.glLineWidth(4);

            shader.forceColor(1f, 1f, 1f, 1f);
            this.linesMesh.render(shader, GL20.GL_LINES);
            shader.resetForcedColor();

            Gdx.gl20.glLineWidth(1);
            if (player.playerController.currentBlockDamage > 0) {
                this.renderBlockBreakingDamage();
            }
        }

        shader.translate(-blockX, -blockY, -blockZ);

        Gdx.gl.glDisable(GL_POLYGON_OFFSET_FILL);
    }

    /**
     * Renders the block breaking progress
     */
    private void renderBlockBreakingDamage() {

        StdShader shader = Minecraft.mc.shaderManager.stdShader;
        Minecraft.mc.textureManager.blockDestroyStages.bind();
        shader.enableTextureMapping();

        shader.setPixelU0(0);
        shader.setPixelU1(16);
        shader.setPixelV0(16 * (int) (player.playerController.currentBlockDamage * 10));
        shader.setPixelV1(160 + 16 + 16 * (int) (player.playerController.currentBlockDamage * 10));

        this.trianglesMesh.render(shader, GL20.GL_TRIANGLES);
        shader.disableTextureMapping();

    }

}
