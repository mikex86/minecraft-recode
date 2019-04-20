package me.gommeantilegit.minecraft.ui.screen.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.font.FontColor;
import me.gommeantilegit.minecraft.font.FontRenderer;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.ui.button.GuiButton;
import me.gommeantilegit.minecraft.ui.screen.GuiScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static me.gommeantilegit.minecraft.AbstractMinecraft.MINECRAFT_VERSION_PREFIX;
import static me.gommeantilegit.minecraft.AbstractMinecraft.MINECRAFT_VERSION_STRING;
import static me.gommeantilegit.minecraft.rendering.Constants.STD_VERTEX_ATTRIBUTES;

public class GuiMainMenu extends GuiScreen {

    /**
     * String Array of all splash texts
     */
    @NotNull
    private static final String[] SPLASH_TEXTS = readSplashes();

    /**
     * Random instance for choosing splash text and the minecraft logo randomization
     */
    @NotNull
    private static final Random RANDOM = new Random();

    /**
     * THe minecraft logo representation string array
     */
    @NotNull
    private static final String MINECRAFT_LOGO[] = RANDOM.nextInt(1000000000) == 999999999 ?
            RANDOM.nextInt(1000000000) == 999999999 ?
                    new String[]{
                            " *   * * **** *  * * *** *** *** *** ***",
                            " ** ** * *    *  * * *   * * * * *    * ",
                            " * * * * *    **** * *   **  *** **   * ",
                            " *   * * *    *  * * *   * * * * *    * ",
                            " *   * * **** *  * * *** * * * * *    * "
                    }
                    :
                    new String[]{
                            " ****  ****** *   * * *** *** *** *** ***",
                            " *     *    * ** ** * *   * * * * *    * ",
                            " ****  ****** * * * * *   **  *** **   * ",
                            "    *  *    * *   * * *   * * * * *    * ",
                            " ****  *    * *   * * *** * * * * *    * "
                    }
            :
            new String[]{
                    " *   * * *   * *** *** *** *** *** ***",
                    " ** ** * **  * *   *   * * * * *    * ",
                    " * * * * * * * **  *   **  *** **   * ",
                    " *   * * *  ** *   *   * * * * *    * ",
                    " *   * * *   * *** *** * * * * *    * "
            };

    /**
     * Currently displayed splash text
     */
    @NotNull
    private final String splashText;

    /**
     * The minecraft logo animation
     */
    private MinecraftLogo minecraftLogo;

    /**
     * String with of displayed splash text
     */
    private int splashTextStringWidth;

    public GuiMainMenu() {
        this.splashText = SPLASH_TEXTS[RANDOM.nextInt(SPLASH_TEXTS.length)];
    }

    /**
     * The Mojang Copyright notice string
     */
    @NotNull
    private final String mojangString = "Copyright Mojang AB. Do not distribute.";

    /**
     * String width of {@link #mojangString}
     */
    private int mojangStringWidth;

    /**
     * Minecraft version string
     * (example: "Minecraft Beta 1.0")
     */
    @NotNull
    private final String minecraftString = "Minecraft " + MINECRAFT_VERSION_PREFIX + " " + MINECRAFT_VERSION_STRING;

    @Override
    public void initGui(int scaledWidth, int scaledHeight) {
        super.initGui(scaledWidth, scaledHeight);
        int yOffset = scaledHeight / 4 + 48;
        this.buttons.add(new GuiButton("Singleplayer", scaledWidth / 2 - 100, yOffset, mc).setOnMouseDownListener(
                pointer -> this.minecraftLogo.reset()
        ));
        this.buttons.add(new GuiButton("Multiplayer", scaledWidth / 2 - 100, yOffset + 24, mc).setOnMouseDownListener(
                pointer -> mc.uiManager.displayGuiScreen(new GuiMultiplayer(this))
        ));

        this.buttons.add(new GuiButton("Mods and Texture Packs", scaledWidth / 2 - 100, yOffset + 48, mc));
        this.buttons.add(new GuiButton("Options...", 98, 20, scaledWidth / 2 - 100, yOffset + 72 + 12, mc).setOnMouseDownListener(pointer -> mc.uiManager.displayGuiScreen(new GuiOptions(this))));
        this.buttons.add(new GuiButton("Quit Game", 98, 20, scaledWidth / 2 + 2, yOffset + 72 + 12, mc).setOnMouseDownListener(pointer -> mc.shutdown()));

        if (this.minecraftLogo == null)
            this.minecraftLogo = new MinecraftLogo();

        this.minecraftLogo.resize(mc.width, mc.height);
        this.mojangStringWidth = mc.uiManager.fontRenderer.getStringWidth(mojangString);
        this.splashTextStringWidth = this.mc.uiManager.fontRenderer.getStringWidth(splashText);
    }


    @Override
    public void render() {
        drawDefaultBackground();
        FontRenderer fontRenderer = mc.uiManager.fontRenderer;
        fontRenderer.drawStringWithShadow(minecraftString, 2, 2, FontColor.DARK_GRAY);
        fontRenderer.drawStringWithShadow(mojangString, DPI.scaledWidthi - mojangStringWidth - 2, DPI.scaledHeighti - 10, FontColor.WHITE);
        super.render();
        this.minecraftLogo.render();
        drawSplashText();
    }

    @Override
    public void tick(float partialTicks) {
        if (minecraftLogo != null)
            this.minecraftLogo.tick(partialTicks);
    }

    /**
     * Renders the minecraft splash text
     */
    private void drawSplashText() {
        FontRenderer fontRenderer = mc.uiManager.fontRenderer;
        int width = DPI.scaledWidthi;
        Matrix4 push = spriteBatch.getTransformMatrix();
        Matrix4 pop = push.cpy();
        push.translate(width / 2f + 90, 70F, 0.0F);
        push.rotate(0.0F, 0.0F, 1.0F, -20F);
        float scale = (float) (1.8F - Math.abs(Math.sin(((float) (System.currentTimeMillis() % 1000L) / 1000F) * 3.141593F * 2.0F) * 0.1F));
        scale = (scale * 100F) / (float) (splashTextStringWidth + 32);
        push.scale(scale, scale, scale);
        fontRenderer.drawStringWithShadow(splashText, -splashTextStringWidth / 2f, -8, 0xffff00);
        spriteBatch.setTransformMatrix(pop);
    }

    /**
     * @return all splash texts by reading the resource file and returning all lines as an array
     */
    @NotNull
    private static String[] readSplashes() {
        return Gdx.files.classpath("text/splashes.txt").readString("UTF-8").split("\r\n");
    }

    private static Mesh stoneBlockMesh;

    private class MinecraftLogo implements Tickable {

        @NotNull
        private final LogoBlock[][] logoBlocks;

        @NotNull
        private final Camera cam = new PerspectiveCamera(70f, 0, 0);

        @NotNull
        private final Vector3 lightDir = new Vector3(
                1, 1, 1
        ).nor();


        private MinecraftLogo() {
            this.logoBlocks = new LogoBlock[MINECRAFT_LOGO.length][MINECRAFT_LOGO[0].length()];
            for (int i = 0; i < logoBlocks.length; i++) {
                for (int j = 0; j < logoBlocks[0].length; j++) {
                    char c = MINECRAFT_LOGO[i].charAt(j);
                    if (c != ' ') {
                        logoBlocks[i][j] = new LogoBlock(i, j);
                    }
                }
            }
            cam.near = 0.05f;
            cam.far = 100f;
        }

        public void resize(int width, int height) {
            int k = 120 * DPI.scaleFactor;
            this.cam.viewportWidth = width;
            this.cam.viewportHeight = height - k;
        }

        public void render() {
            if (stoneBlockMesh == null) {
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(STD_VERTEX_ATTRIBUTES, GL_TRIANGLES);
                Objects.requireNonNull(mc.blockRendererRegistry.getRenderer(mc.blocks.stone)).render(meshBuilder, 0, 0, 0, 0, 0, 0, null, mc.blocks.stone.getDefaultBlockState(), true);
                stoneBlockMesh = meshBuilder.end();
            }

            Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
            Gdx.gl20.glCullFace(GL20.GL_BACK);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            int k = 120 * DPI.scaleFactor;
            Gdx.gl.glViewport(0, mc.height - k, mc.width, k);

            StdShader stdShader = mc.shaderManager.stdShader;
            stdShader.begin();
            stdShader.setUniformf("lightDirection", lightDir);
            stdShader.setVirtualCameraPos(0, 0, 0);
            cam.update();
            stdShader.setCamera(cam);
            stdShader.renderStart();
            stdShader.disableLighting();
            stdShader.enableLighting();
            stdShader.translate(0.0F, 1.1F, -13F);
            stdShader.scale(1.0F, -1F, 1.0F);
            stdShader.rotate(1.0F, 0.0F, 0.0F, 15F);
            stdShader.scale(0.71F, 0.98F, 0.4F);
            stdShader.translate((float) (-MINECRAFT_LOGO[0].length()) * 0.5F, (float) (-MINECRAFT_LOGO.length) * 0.5F, 0.0F);

            for (int layer = 0; layer < 2; layer++) {
                stdShader.pushMatrix();
                if (layer == 0) {
                    Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);
                    stdShader.translate(0.0F, -0.6F, 0.0F);
                    stdShader.translate(0.0F, 1.0F, -1.0F);
                } else {
//                    stdShader.setColorAdd(0.1f, 0.1f, 0.1f, 0);
                    mc.textureManager.blockTextureMap.getTexture().bind();
                }
                for (int y = 0; y < logoBlocks.length; y++) {
                    for (int x = 0; x < logoBlocks[y].length; x++) {
                        LogoBlock logoBlock = this.logoBlocks[y][x];
                        if (logoBlock == null)
                            continue;
                        stdShader.pushMatrix();
                        float translateZ = (float) (logoBlock.lastTranslation + (logoBlock.translation - logoBlock.lastTranslation) * (double) mc.timer.partialTicks);
                        float scale = 1.0F;
                        float alpha = 1.0F;
                        float rotate = 0.0F;
                        if (layer == 0) {
                            scale = translateZ * 0.04F + 1.0F;
                            alpha = 1.0F / scale;
                            translateZ = 0.0F;
                        }
                        stdShader.translate(x, y, translateZ);
                        stdShader.scale(scale, scale, scale);
                        stdShader.rotate(0.0F, 1.0F, 0.0F, rotate);
                        {
                            if (layer == 0)
                                stdShader.setColor(1f, 1f, 1f, alpha);
                            else
                                stdShader.setColor(1.51f, 1.51f, 1.51f, alpha);
                            stoneBlockMesh.render(stdShader, GL_TRIANGLES);
                        }
                        stdShader.popMatrix();
                    }
                }
                stdShader.popMatrix();
            }
            stdShader.setColor(1f, 1f, 1f, 1f);
            stdShader.renderEnd();
            stdShader.end();
            Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
            Gdx.gl20.glViewport(0, 0, mc.width, mc.height);
        }

        public void reset() {
            for (int i = 0; i < this.logoBlocks.length; i++) {
                for (int j = 0; j < this.logoBlocks[i].length; j++) {
                    if (this.logoBlocks[i][j] != null)
                        this.logoBlocks[i][j].reset();
                }
            }
        }

        /**
         * Called to update the logo animation
         *
         * @param partialTicks the ticks to be performed in the current frame being rendered.
         */
        @Override
        public void tick(float partialTicks) {
            for (int i = 0; i < this.logoBlocks.length; i++) {
                for (int j = 0; j < this.logoBlocks[i].length; j++) {
                    if (this.logoBlocks[i][j] != null)
                        this.logoBlocks[i][j].updateLogoBlock();
                }
            }
        }

        private class LogoBlock {

            private final int x, y;
            private double translation, lastTranslation, smooth;

            public LogoBlock(int x, int y) {
                this.x = x;
                this.y = y;
                translation = lastTranslation = (double) (10 + y) + RANDOM.nextDouble() * 32D + (double) x;
            }

            /**
             * Updates the position of the logo block
             */
            public void updateLogoBlock() {
                float delta = 1;
                lastTranslation = translation;
                if (translation > 0.0D) {
                    smooth -= 0.59999999999999998D * delta;
                }
                translation += smooth;
                smooth *= 0.90000000000000002D * delta;
                if (translation < 0.0D) {
                    translation = 0.0D;
                    smooth = 0.0D;
                }
            }

            public void reset() {
                translation = lastTranslation = (double) (10 + y) + RANDOM.nextDouble() * 32D + (double) x;
            }
        }
    }
}
