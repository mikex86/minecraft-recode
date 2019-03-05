package me.gommeantilegit.minecraft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.annotations.AndroidOnly;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.hud.IngameHud;
import me.gommeantilegit.minecraft.input.GameInput;
import me.gommeantilegit.minecraft.input.InputHandler;
import me.gommeantilegit.minecraft.particle.ParticleEngine;
import me.gommeantilegit.minecraft.profiler.Profiler;
import me.gommeantilegit.minecraft.shader.ShaderManager;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.texture.manager.TextureManager;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import me.gommeantilegit.minecraft.timer.tick.MinecraftThread;
import me.gommeantilegit.minecraft.world.World;
import me.gommeantilegit.minecraft.world.generation.generator.WorldGenerator;
import me.gommeantilegit.minecraft.world.generation.generator.options.WorldGenerationOptions;
import me.gommeantilegit.minecraft.world.saveformat.LevelLoader;
import me.gommeantilegit.minecraft.world.saveformat.LevelSaver;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static java.lang.Math.max;

public class Minecraft implements ApplicationListener, Tickable, OpenGLOperation {

    @NotNull
    public static Minecraft mc;

    @NotNull
    public final ShaderManager shaderManager = new ShaderManager();

    @NotNull
    public final VertexAttributes vertexAttributes = new VertexAttributes(

            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_Color"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_Normal"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_TextureCoord")

    );

    /**
     * Handler object for handling input listeners.
     */
    @NotNull
    public final InputHandler inputHandler = new InputHandler();

    /**
     * Direction of ambient light
     */
    @NotNull
    private Vector3 ambientLightDirection = new Vector3(1, 2, 5).nor();

    /**
     * The current world instance.
     */
    public World theWorld;

    /**
     * Width and height of the display.
     */
    public int width, height;

    /**
     * The player instance
     */
    public Player thePlayer;

    /**
     * Rendering thread (OpenGL Context thread instance)
     */
    public final Thread renderingThread;

    /**
     * IngameHud rendering object
     */
    public IngameHud ingameHud;

    /**
     * SpriteBatch instance to use for 2D rendering. Updated by {@link #ingameHud} (Adjusted to change of resolution etc)
     */
    public SpriteBatch spriteBatch;

    /**
     * Manager object for loading textures and combining texture wrappers
     */
    public TextureManager textureManager;

    /* Profiler */

    /**
     * Profiler object for profiling the tick update.
     */
    public final Profiler tickProfiler = new Profiler("Game-Tick", false);

    /**
     * Profiler object for profiling the tick update.
     */
    private final Profiler startupProfiler = new Profiler("Game-startup", false);

    /* Profiler end */

    /**
     * Timer instance for timing game logic.
     */
    @NotNull
    public Timer timer = new Timer(20.0f);

    /**
     * Timer instance for time OpenGL Context calls
     */
    @NotNull
    public Timer openGLOperationsTimer = new Timer(10.0f);

    /**
     * Object used for spawning particle structures into the world.
     */
    public ParticleEngine particleEngine;

    /**
     * State if the game is loaded.
     */
    private boolean loaded;

    @NotNull
    public final MinecraftThread minecraftThread = new MinecraftThread(this);

    public Minecraft() {
        mc = this;
        renderingThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(this::onGameClosed));
    }

    /**
     * Creating the game.
     */
    @Override
    public void create() {

        if (!loaded)
            loadGame();
        else resumeAndroid();

        Gdx.input.setInputProcessor(inputHandler);
        System.out.println("Created game.");
    }

    /**
     * Loads the game.
     */
    private void loadGame() {
        startupProfiler.actionStart();

        startupProfiler.actionStart("Compile shaders");
        shaderManager.compileShaders();
        startupProfiler.actionEnd("Compile shaders");

        this.spriteBatch = new SpriteBatch();

        startupProfiler.actionStart("Load Textures");
        //Initializing textures
        this.textureManager = new TextureManager(spriteBatch);

        startupProfiler.actionEnd("Load Textures");

        //Initializing Blocks - Must be called after texture manger initialization -> access to textureManager -> building texture atlas
        Blocks.init();

        this.ingameHud = new IngameHud(spriteBatch);
        this.ingameHud.resize(width, height);

        startupProfiler.actionStart("World loading");

        thePlayer = new Player(null);
        thePlayer.setPosition(0, 50, 10);

        /* Test Only */
        {
            String levelFileName = "saves/test_save.lvl";
            if (!new File(levelFileName).exists()) {
                WorldGenerator worldGenerator = new WorldGenerator(123, WorldGenerator.WorldType.OVERWORLD, new WorldGenerationOptions(false));
                theWorld = new World(thePlayer, worldGenerator, null, 256);
            } else {
                try {
                    LevelLoader levelLoader = new LevelLoader(new DataInputStream(new FileInputStream(levelFileName)));
                    try {
                        theWorld = levelLoader.loadWorld(thePlayer);
                    } catch (Exception e) {
                        throw new RuntimeException("Loading of world fails. (NBT Reading)", e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Level file not found!", e);
                }
            }
        }
        /* Test Only End */
        particleEngine = new ParticleEngine(this.theWorld);

        startupProfiler.actionEnd("World loading");

        this.inputHandler.registerInputProcessor(new GameInput());
        this.inputHandler.registerInputProcessor(mc.thePlayer.camController);
        theWorld.spawnEntityInWorld(thePlayer);

        startupProfiler.actionEnd();
        startupProfiler.printResults();
        loaded = true;
        this.minecraftThread.start(); //Starting Tick Thread.
    }

    /**
     * Resizing the game
     *
     * @param width  display width in pixel
     * @param height display height in pixel
     */
    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        mc.thePlayer.camera.viewportWidth = width;
        mc.thePlayer.camera.viewportHeight = height;
        this.ingameHud.resize(width, height);
    }

    /**
     * Updates the game
     *
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    @Override
    public void tick(float partialTicks) {
        tickProfiler.actionStart();
        theWorld.tick(partialTicks);
        timer.tick(partialTicks);
        tickProfiler.actionEnd();
    }

    /**
     * Called on OpenGL Context to update timer
     */
    @Override
    public void onOpenGLContext(float partialTicks) {
        this.particleEngine.onOpenGLContext(partialTicks);
        this.theWorld.onOpenGLContext(partialTicks);
    }

    private boolean firstRender = true;

    @NotNull
    private final Profiler renderProfiler = new Profiler("Rendering-Profiler", false);

    @Override
    public void render() {

        renderProfiler.actionStart();

        if (firstRender) {
            System.out.println("Rendering the game");
            firstRender = false;
        }

        //Game Rendering
        {

            Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
            Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);

            Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            Gdx.gl.glClearColor(0.3294118f, 0.60784315f, 0.72156863f, 1f);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            Gdx.gl20.glCullFace(GL20.GL_FRONT);

            float partialTicks = timer.partialTicks;
            renderProfiler.actionStart("PlayerUpdate");

            this.thePlayer.updateCameraController(partialTicks);
            this.thePlayer.updateCamera();
            renderProfiler.actionEnd("PlayerUpdate");

            shaderManager.stdShader.begin();
            shaderManager.stdShader.setUniformf("lightDirection", this.ambientLightDirection);
            shaderManager.stdShader.updateMatrix();

            renderProfiler.actionStart("WorldRendering");
            theWorld.render(partialTicks);
            renderProfiler.actionEnd("WorldRendering");

            renderProfiler.actionStart("RayTracing");
            this.thePlayer.rayTracer.update();
            renderProfiler.actionEnd("RayTracing");

            this.thePlayer.renderCamControllerOverlay();

            shaderManager.stdShader.newFrame();
            shaderManager.stdShader.end();

            renderProfiler.actionStart("IngameHud");
            this.ingameHud.render();
            renderProfiler.actionEnd("IngameHud");
        }

        renderProfiler.actionStart("OpenGlOperations");
        //Updating timer and ticks
        {
            openGLOperationsTimer.advanceTime();
            for (int i = 0; i < openGLOperationsTimer.ticks; i++) {
                onOpenGLContext(mc.openGLOperationsTimer.partialTicks);
            }
        }
        renderProfiler.actionEnd("OpenGlOperations");
        renderProfiler.actionEnd();
    }

    /**
     * Profiler instance for profiling the reload performed on app resume on android
     */
    @NotNull
    private final Profiler resumeProfiler = new Profiler("Android-Resume", false);

    /**
     * Method only called on mobile to reinitialize the game after OpenGL context loss
     */
    @AndroidOnly
    public void resumeAndroid() {
        resumeProfiler.actionStart();
        shaderManager.compileShaders();
        resumeProfiler.actionStart("Recreating textures");
        CustomTexture.recreateTextures();

        //Reinitializing the sprite-batch shader. (Program is not valid any more after context loss)
        this.spriteBatch.setShader(SpriteBatch.createDefaultShader());

        this.ingameHud.resize(width, height);

        resumeProfiler.actionEnd("Recreating textures");
        theWorld.invalidateMeshes();
        resumeProfiler.actionEnd();
        resumeProfiler.printResults();
    }

    /**
     * Called on game termination
     */
    private void onGameClosed() {
        /* Test Only */
        LevelSaver levelSaver = new LevelSaver(theWorld);
        try {
            // TODO: REWRITE LEVEL SAVING / LOADING
            new File("saves").mkdirs();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            levelSaver.save(new DataOutputStream(byteOut));
            new FileOutputStream("saves/test_save.lvl").write(byteOut.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Saving of level failed", e);
        }
        /* Test Only End */
        this.tickProfiler.printResults();
        this.theWorld.getChunkCreator().chunkCreatorProfiler.printResults();
        this.renderProfiler.printResults();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {

    }

    public boolean isRunning() {
        return this.renderingThread.isAlive();
    }
}