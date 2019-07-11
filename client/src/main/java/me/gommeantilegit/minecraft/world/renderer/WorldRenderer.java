package me.gommeantilegit.minecraft.world.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.timer.api.AsyncOperation;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import me.gommeantilegit.minecraft.world.chunk.world.ChunkRenderManager;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.Color.argb8888ToColor;
import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.clamp;
import static java.lang.Math.*;
import static me.gommeantilegit.minecraft.Side.CLIENT;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;
import static me.gommeantilegit.minecraft.util.RenderUtils.rect;

@SideOnly(side = CLIENT)
public class WorldRenderer implements AsyncOperation, OpenGLOperation {

    private static final float CLOUD_LEVEL = 127;

    /**
     * Fog Color
     */
    @NotNull
    private final Color fogColor = new Color(0.3294118f, 0.60784315f, 0.72156863f, 1f);

    /**
     * Parent world to store
     */
    @NotNull
    private final ClientWorld world;

    /**
     * Player viewing the world
     */
    @NotNull
    private final EntityPlayerSP viewer;

    /**
     * Minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * State whether fog should be rendered to smooth of world render limit
     */
    private boolean enableFog;

    /**
     * State whether clouds should be rendered
     */
    private boolean renderClouds;

    /**
     * @param world sets {@link #world}
     * @param viewer sets {@link #viewer}
     * @param mc sets {@link #mc}
     */
    public WorldRenderer(@NotNull ClientWorld world, @NotNull EntityPlayerSP viewer, @NotNull ClientMinecraft mc) {
        this.world = world;
        this.mc = mc;
        this.viewer = viewer;
        setEnableFog(mc.gameSettings.videoSettings.getEnableFog());
        setRenderClouds(mc.gameSettings.videoSettings.getRenderClouds());
    }

    /**
     * Renders the world
     *
     * @param partialTicks timer partial ticks
     */
    public void render(float partialTicks) {
        Gdx.gl20.glDisable(GL_CULL_FACE); // Disable Face culling!!! (Or else white artifacts at T-Junctions will appear)
        long worldTime = world.worldTime;
        StdShader shader = mc.shaderManager.stdShader;
        shader.enableFog(enableFog);
        updateFogColor(worldTime, partialTicks);

        if (enableFog) {
            setupFog();
            shader.setFogColor(fogColor);
            Gdx.gl20.glCullFace(GL20.GL_BACK);
            renderBlueSky(worldTime, partialTicks);
        }

        setupWorldLighting(worldTime, partialTicks);
        {
            // Rendering Blocks
            this.world.chunkRenderManager.startStage(ChunkRenderManager.RenderStage.CHUNKS);
//            synchronized (this.world.chunkRenderManager.chunks) {
            {
                while (this.world.chunkRenderManager.hasNext()) {
                    ClientChunk chunk = this.world.chunkRenderManager.nextChunk();
                    if (chunk != null) {
                        if (isChunkInCameraFrustum(chunk)) {
                            try {
                                mc.textureManager.blockTextureMap.getTexture().bind();
                                chunk.render(partialTicks);
                            } catch (Exception e) {
                                throw new IllegalStateException("Error while rendering chunk " + chunk, e);
                            }
                        }
                    }
                }
            }

            if (renderClouds) {
                // Rendering Clouds
                setupCloudLighting(worldTime, partialTicks);
                renderClouds(worldTime, partialTicks);
                shader.setColor(1, 1, 1, 1);
            }
        }

        shader.enableFog(false);
        Gdx.gl.glEnable(GL_CULL_FACE);
        Gdx.gl.glCullFace(GL_FRONT); // Resetting culled face to Front face
    }

    /**
     * Sets up the shader lighting uniforms for world rendering
     *
     * @param worldTime    the world time in ticks
     * @param partialTicks timer partial ticks
     */
    public void setupWorldLighting(long worldTime, float partialTicks) {
        float celestialAngle = calculateCelestialAngle(worldTime, partialTicks);
        float brightness = (float) (cos(celestialAngle * 3.141593F * 2.0F) * 2.0F + 0.5F);
        brightness = clamp(brightness, 0, 1);
        double angle = 2 * PI * celestialAngle;
        Vector3 lightDir = new Vector3((float) cos(angle), (float) sin(angle), 0);
        mc.shaderManager.stdShader.setUniformf("lightDirection", lightDir);
        mc.shaderManager.stdShader.setMinDiffusedLighting(0.2f);
        mc.shaderManager.stdShader.setColor(brightness, brightness, brightness, 1);
    }

    /**
     * Sets up the shader lighting uniforms for cloud rendering
     *
     * @param worldTime    the world time in ticks
     * @param partialTicks timer partial ticks
     */
    public void setupCloudLighting(long worldTime, float partialTicks) {
        Color fogColor = getFogColor(worldTime, partialTicks);
        mc.shaderManager.stdShader.setUniformf("lightDirection", 0.20000000298023224f, -1.0f, -0.69999998807907104f); // Setting up cloud lighting
        mc.shaderManager.stdShader.setMinDiffusedLighting(0.6f);
        mc.shaderManager.stdShader.setColor(fogColor.r, fogColor.g, fogColor.b, 0.8f);
    }

    /**
     * @param worldTime    the world time in ticks
     * @param partialTicks the relative value of the current tick that has passed
     * @return the angle for sun/moon according to the world time
     */
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        int i = (int) (worldTime % 24000L);
        float f1 = ((float) i + partialTicks) / 24000F - 0.25F;
        if (f1 < 0.0F) {
            f1++;
        }
        if (f1 > 1.0F) {
            f1--;
        }
        float f2 = f1;
        f1 = 1.0F - (float) ((cos((double) f1 * 3.1415926535897931D) + 1.0D) / 2D);
        f1 = f2 + (f1 - f2) / 3F;
        return f1;
    }

    /**
     * calculates fog and calls glClearColor
     *
     * @param worldTime    the world time in ticks
     * @param partialTicks timer partial ticks
     */
    private void updateFogColor(long worldTime, float partialTicks) {
//        WorldBase world = mc.theWorld;
//        LivingEntity entityliving = mc.thePlayer;
        float dstFactor = 0.25F + 0.75F * (float) this.world.getChunkLoadingDistance() / ChunkBase.CHUNK_SIZE / 32.0F;
        dstFactor = 1.0F - (float) Math.pow((double) dstFactor, 0.25D);
        Color skyColor = getSkyColor(worldTime, partialTicks);
        float skyR = skyColor.r;
        float skyG = skyColor.g;
        float skyB = skyColor.b;
        Color vec3d1 = getFogColor(worldTime, partialTicks);
        float fogColorRed = vec3d1.r;
        float fogColorGreen = vec3d1.g;
        float fogColorBlue = vec3d1.b;
        fogColorRed += (skyR - fogColorRed) * dstFactor;
        fogColorGreen += (skyG - fogColorGreen) * dstFactor;
        fogColorBlue += (skyB - fogColorBlue) * dstFactor;
        // TODO: IMPLEMENT WHEN FLUIDS ARE IMPLEMENTED
//        if (entityliving.isInsideOfMaterial(Material.water)) {
//            fogColorRed = 0.02F;
//            fogColorGreen = 0.02F;
//            fogColorBlue = 0.2F;
//        } else if (entityliving.isInsideOfMaterial(Material.lava)) {
//            fogColorRed = 0.6F;
//            fogColorGreen = 0.1F;
//            fogColorBlue = 0.0F;
//        }
//        float rainStrength = 0; // Temporary rain strength. TODO

        //TODO: UNCOMMENT WHEN RAIN IS IMPLEMENTED
//        if (rainStrength > 0.0F) {
//            float redGreenComponent = 1.0F - rainStrength * 0.5F;
//            float blueComponent = 1.0F - rainStrength * 0.4F;
//            fogColorRed *= redGreenComponent;
//            fogColorGreen *= redGreenComponent;
//            fogColorBlue *= blueComponent;
//        }

        // Clearing background with fog color
        Gdx.gl.glClearColor(fogColorRed, fogColorGreen, fogColorBlue, 0.0F);
        this.fogColor.set(fogColorRed, fogColorGreen, fogColorBlue, 1);
    }

    /**
     * Rectangle hovering above the player to achieve a blue sky top
     */
    private static Mesh blueSkyMesh;

    /**
     * Renders a big rect to make the sky top blue
     *
     * @param worldTime    the world time in ticks
     * @param partialTicks timer partial ticks
     */
    private void renderBlueSky(long worldTime, float partialTicks) {
        // Building the sky top mesh
        if (blueSkyMesh == null) {
            int len = 512;
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.begin(STD_VERTEX_ATTRIBUTES, GL_TRIANGLES);
            meshBuilder.circle(len, 10, 0, 0, 0, 0, -1, 0);
            blueSkyMesh = meshBuilder.end();
        }
        Color skyColor = getSkyColor(worldTime, partialTicks);
        StdShader shader = mc.shaderManager.stdShader;

        Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);
        Gdx.gl.glDisable(GL_TEXTURE_2D);

        shader.disableLighting();
        shader.forceColor(skyColor.r, skyColor.g, skyColor.b, skyColor.a);
        shader.pushMatrix();
        shader.translate(viewer.posX, viewer.lastPosY + (viewer.posY - viewer.lastPosY) * partialTicks + CLOUD_LEVEL, viewer.posZ);
        blueSkyMesh.render(shader, GL_TRIANGLES);
        shader.popMatrix();
        shader.resetForcedColor();
        shader.resetForcedColor();
        shader.enableLighting();
        Gdx.gl.glEnable(GL_TEXTURE_2D);
    }

    /**
     * Sets up the fog
     */
    private void setupFog() {
        StdShader shader = mc.shaderManager.stdShader;
        float farPlaneDistance = world.getChunkLoadingDistance();
        shader.setFogStart(farPlaneDistance * 0.25F);
        shader.setFogEnd(farPlaneDistance);
    }

    /**
     * @param worldTime    the world time in ticks
     * @param partialTicks the relative time (unit tick) that has passed of the current time
     * @return the fog color according to world time
     */
    @NotNull
    private Color getFogColor(long worldTime, float partialTicks) {
        float celestialAngle = calculateCelestialAngle(worldTime, partialTicks);
        float brightness = (float) (cos(celestialAngle * 3.141593F * 2.0F) * 2.0F + 0.5F);
        if (brightness < 0.0F) {
            brightness = 0.0F;
        }
        if (brightness > 1.0F) {
            brightness = 1.0F;
        }
        float r = 0.7529412F;
        float g = 0.8470588F;
        float b = 1.0F;
        r *= brightness * 0.94F + 0.06F;
        g *= brightness * 0.94F + 0.06F;
        b *= brightness * 0.91F + 0.09F;
        return new Color(r, g, b, 1);
    }

    private static Mesh cloudMesh;

    /**
     * Renders the clouds of the sky
     *
     * @param worldTime    the world time in ticks
     * @param partialTicks timer partial ticks
     */
    private void renderClouds(long worldTime, float partialTicks) {
        int cloudSize = 12;
        Gdx.gl.glDisable(GL_CULL_FACE);
        mc.textureManager.cloudsTexture.bind();
        int size = mc.textureManager.cloudsTexture.getWidth() * cloudSize; // Each pixel is 12 block wide (12 block = minecraft cloud width/depth)
        if (cloudMesh == null) {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.begin(STD_VERTEX_ATTRIBUTES, GL_TRIANGLES);
            rect(meshBuilder,
                    -size, 0, -size,
                    0, 0,
                    size, 0, -size,
                    1, 0,
                    size, 0, size,
                    1, 1,
                    -size, 0, size,
                    0, 1,
                    0, -1, 0,
                    1, 1, 1, 1

            );
            cloudMesh = meshBuilder.end();
        }
        mc.shaderManager.stdShader.disableLighting();
        mc.shaderManager.stdShader.pushMatrix();

        float cloudMovingSpeed = 250f;
        float movingXTranslate = (((worldTime + partialTicks) % cloudMovingSpeed) / cloudMovingSpeed) * cloudSize * 2;

        float viewX = viewer.lastPosX + (viewer.posX - viewer.lastPosX) * partialTicks;
        float viewZ = viewer.lastPosZ + (viewer.posZ - viewer.lastPosZ) * partialTicks;

        int xC = (int) (viewX / cloudSize);
        int zC = (int) (viewZ / cloudSize);

        mc.shaderManager.stdShader.translate(-movingXTranslate + xC * cloudSize * 2, CLOUD_LEVEL, zC * cloudSize * 2);
        mc.shaderManager.stdShader.enableTextureMapping();

        int xOffset = (int) (worldTime / cloudMovingSpeed);

        mc.shaderManager.stdShader.setPixelU0(xC + xOffset);
        mc.shaderManager.stdShader.setPixelU1(xC + xOffset + mc.textureManager.cloudsTexture.getWidth());
        mc.shaderManager.stdShader.setPixelV0(zC);
        mc.shaderManager.stdShader.setPixelV1(zC + mc.textureManager.cloudsTexture.getHeight());
        cloudMesh.render(mc.shaderManager.stdShader, GL_TRIANGLES);

        mc.shaderManager.stdShader.disableTextureMapping();
        mc.shaderManager.stdShader.popMatrix();
        mc.shaderManager.stdShader.enableLighting();
        Gdx.gl.glEnable(GL_CULL_FACE);
    }

    /**
     * @param worldTime    the world time in ticks
     * @param partialTicks the relative value of the current tick that has passed
     * @return the world sky color vector according to the world time (brightness variation according to world time).
     */
    @NotNull
    private Color getSkyColor(long worldTime, float partialTicks) {
//        Entity entity = viewer;
        float angle = calculateCelestialAngle(worldTime, partialTicks);
        float brightness = (float) (cos(angle * Math.PI * 2.0F) * 2.0F + 0.5F);
        if (brightness < 0.0F) {
            brightness = 0.0F;
        }
        if (brightness > 1.0F) {
            brightness = 1.0F;
        }
//        int x = (int) floor(entity.posX);
//        int z = (int) floor(entity.posZ);
        float temperature = 1f; // Temperature is one for now TODO
        return getSkyColorByTemperature(temperature).mul(brightness);
    }

    /**
     * @param temperature the temperature of a block position
     * @return the sky color at that block position
     */
    @NotNull
    private Color getSkyColorByTemperature(float temperature) {
        temperature /= 3F;
        if (temperature < -1F) {
            temperature = -1F;
        }
        if (temperature > 1.0F) {
            temperature = 1.0F;
        }
        Color color = new Color();
        argb8888ToColor(color, java.awt.Color.getHSBColor(0.6222222F - temperature * 0.05F, 0.5F + temperature * 0.1F, 1.0F).getRGB());
        return color;
    }

    /**
     * @param chunk the given chunk to be checked
     * @return true if the chunk is in the players frustum
     */
    private boolean isChunkInCameraFrustum(@NotNull ChunkBase chunk) {
        return this.viewer.virtualCamera.frustum.boundsInFrustum(chunk.getBoundingBox());
    }

    public ClientWorld getWorld() {
        return world;
    }

    @Override
    public void onAsyncThread() {
    }

    @Override
    public void onOpenGLContext(float partialTicks) {
    }

    public void setEnableFog(boolean enableFog) {
        this.enableFog = enableFog;
    }

    public void setRenderClouds(boolean renderClouds) {
        this.renderClouds = renderClouds;
    }
}
