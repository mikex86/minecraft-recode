package me.gommeantilegit.minecraft.world.renderer;

import com.badlogic.gdx.graphics.Color;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class WorldRenderer {

    /**
     * Fog Color Constant
     */
    @NotNull
    private static final Color FOG_COLOR = new Color(0.3294118f, 0.60784315f, 0.72156863f, 1f);

    /**
     * Parent world to store
     */
    @NotNull
    private final World world;

    /**
     * Player viewing the world
     */
    @NotNull
    private final Player viewer;

    /**
     * The maximum distance to a chunk that can be loaded
     */
    private int renderDistance = 64;

    /**
     * @param world sets {@link #world}
     */
    public WorldRenderer(@NotNull World world, @NotNull Player viewer) {
        this.world = world;
        this.viewer = viewer;
    }

    /**
     * Renders the world
     *
     * @param partialTicks delta time
     */
    public void render(float partialTicks) {
        StdShader shader = Minecraft.mc.shaderManager.stdShader;
        shader.enableFog(true);
        shader.setFogDensity((float) (1.0 / (renderDistance - 5)));
        shader.setFogGradient(3.5f);
        shader.setFogColor(FOG_COLOR);

        //Rendering Blocks
        {
            this.world.getWorldChunkHandler().getChunkRenderManager().newFrame();
            while (this.world.getWorldChunkHandler().getChunkRenderManager().hasNext()) {
                Chunk chunk = this.world.getWorldChunkHandler().getChunkRenderManager().nextChunk();
                if (chunk != null && isChunkInCameraFrustum(chunk)) {
                    try {
                        Minecraft.mc.textureManager.blockTextureMap.getTexture().bind();
                        chunk.render(partialTicks);
                    } catch (Exception e) {
                        throw new IllegalStateException("Error while rendering chunk " + chunk, e);
                    }
                }
            }
        }

        shader.enableFog(false);

    }

    /**
     * @param chunk the given chunk to be checked
     * @return true if the chunk is in the players frustum
     */
    private boolean isChunkInCameraFrustum(@NotNull Chunk chunk) {
        return this.viewer.camera.frustum.boundsInFrustum(chunk.getBoundingBox());
    }

    public World getWorld() {
        return world;
    }

    public WorldRenderer setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
        world.chunkLoader.setRenderDistance(renderDistance);
        return this;
    }

    public int getRenderDistance() {
        return renderDistance;
    }
}
