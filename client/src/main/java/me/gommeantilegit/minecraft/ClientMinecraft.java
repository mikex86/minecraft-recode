package me.gommeantilegit.minecraft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import me.gommeantilegit.minecraft.annotations.AndroidOnly;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.ClientBlockRendererTypeRegistry;
import me.gommeantilegit.minecraft.block.sound.BlockSounds;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.entity.player.skin.ClientSkin;
import me.gommeantilegit.minecraft.entity.renderer.EntityRenderer;
import me.gommeantilegit.minecraft.gamesettings.GameSettings;
import me.gommeantilegit.minecraft.hud.IngameHud;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.input.GameInput;
import me.gommeantilegit.minecraft.input.InputHandler;
import me.gommeantilegit.minecraft.localization.StringTranslate;
import me.gommeantilegit.minecraft.music.MusicTicker;
import me.gommeantilegit.minecraft.netty.NettyClient;
import me.gommeantilegit.minecraft.profiler.Profiler;
import me.gommeantilegit.minecraft.rendering.GLContext;
import me.gommeantilegit.minecraft.shader.ShaderManager;
import me.gommeantilegit.minecraft.sound.SoundResource;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.texture.manager.TextureManager;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.ui.UIManager;
import me.gommeantilegit.minecraft.ui.screen.impl.GuiMainMenu;
import me.gommeantilegit.minecraft.utils.OpenGLUtils;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.saveformat.ChunkFragmenter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.FutureTask;

import static com.badlogic.gdx.graphics.GL20.GL_NO_ERROR;
import static java.lang.Integer.min;

@SideOnly(side = Side.CLIENT)
public abstract class ClientMinecraft extends AbstractMinecraft implements ApplicationListener, OpenGLOperation {

    /**
     * Shader manager instance
     */
    public ShaderManager shaderManager;

    /**
     * Handler object for handling input listeners.
     */
    public InputHandler inputHandler;

    /**
     * The current world instance.
     */
    public ClientWorld theWorld;

    /**
     * Width and height of the display.
     */
    public int width, height;

    /**
     * The SP player instance
     */
    public EntityPlayerSP thePlayer;

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

    /**
     * Profiler object for profiling the tick update.
     */
    private Profiler startupProfiler;

    /**
     * Timer instance for time OpenGL Context calls
     */
    @NotNull
    public Timer openGLOperationsTimer = new Timer(10.0f);

    /**
     * Object for rendering entities
     */
    public EntityRenderer entityRenderer;

    /**
     * State if the game is loaded.
     */
    private boolean loaded;

    /**
     * Netty Client thread
     */
    public NettyClient nettyClient;

    /**
     * Manger object for handling GuiScreens
     */
    public UIManager uiManager;

    /**
     * State whether the game is running or not
     */
    private boolean running = true;

    /**
     * {@link GameSettings} instance
     */
    public GameSettings gameSettings;

    /**
     * ShapeRenderer instance for 2D rendering
     */
    public ShapeRenderer shapeRenderer;

    /**
     * The registry of block renderers
     */
    public ClientBlockRendererTypeRegistry blockRendererRegistry;

    /**
     * Object storing block instances with their parent sound types
     */
    public BlockSounds blockSounds;

    /**
     * Minecraft string translate instance
     */
    public StringTranslate stringTranslate;

    /**
     * Object deciding what music to play when
     */
    private MusicTicker musicTicker;

    public ClientMinecraft() {
        super(Side.CLIENT);
        GLContext.initGLContext();
    }

    /**
     * Thread that loads the game
     */
    private Thread startUpThread;

    /**
     * Creating the game.
     */
    @Override
    public void create() {
        if (!loaded)
            try {
                startUpThread = new Thread(this::loadGame);
                startUpThread.setDaemon(true);
                startUpThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
                startUpThread.start();
            } catch (Throwable t) {
                this.getLogger().crash("Failed to load game!", t);
            }
        else try {
            resumeAndroid();
        } catch (Throwable t) {
            this.getLogger().crash("Failed to resume game (Android)", t);
        }

        this.getLogger().info("Created game.");
    }

    /**
     * Loads the game.
     */
    protected void loadGame() {
        try {
            this.startupProfiler = new Profiler("Game-startup", false); // Initializing start-up profiler
            super.loadGame();
            Runtime.getRuntime().addShutdownHook(new Thread(this::onGameClosed));

            this.stringTranslate = new StringTranslate(); // Initializing string translator

            this.inputHandler = new InputHandler(); // Initializing input handler
            Gdx.input.setInputProcessor(inputHandler);

            // Initializing sprite batch for 2D rendering
            GLContext glContext = GLContext.getGlContext();
            glContext.runOnGLContextWait(new FutureTask<Void>(() -> {
                {
                    this.shapeRenderer = new ShapeRenderer(); // Initializing 2D Shape renderer
                    this.shapeRenderer.getProjectionMatrix().setToOrtho(0, DPI.scaledWidthi, DPI.scaledHeighti, 0, 0, 1);
                    this.shapeRenderer.updateMatrices();
                }
                if (spriteBatch == null) {
                    this.spriteBatch = new SpriteBatch(); // Initializing 2D Sprite batch texture renderer
                    this.spriteBatch.getProjectionMatrix().setToOrtho(0, DPI.scaledWidthi, DPI.scaledHeighti, 0, 0, 1);
                }
                return null;
            }));

            glContext.runOnGLContextWait(new FutureTask<Void>(() -> {
                this.startupProfiler.actionStart();
                this.startupProfiler.actionStart("Compile shaders");
                this.shaderManager = new ShaderManager();
                this.shaderManager.compileShaders();
                this.startupProfiler.actionEnd("Compile shaders");
                return null;
            }));

            glContext.runOnGLContextWait(new FutureTask<Void>(() -> {
                // Initializing UiManager for GuiScreen handling
                this.uiManager = new UIManager(spriteBatch, this);
                return null;
            }));

            this.setBlocks(new Blocks(this));
            // Initializing Blocks - Must be called before texture manger initialization -> access to textureManager -> building texture atlas
            this.getBlocks().init();

            this.blockSounds = new BlockSounds(); // Initializing block sounds
            this.blockSounds.init(getBlocks());
            this.blockRendererRegistry = new ClientBlockRendererTypeRegistry(this, getBlocks()); // Needs to be initialized before instantiation of TextureManager

            this.chunkFragmenter = new ChunkFragmenter(this);

            glContext.runOnGLContextWait(new FutureTask<Void>(() -> {
                this.startupProfiler.actionStart("Load Textures");

                //Initializing textures
                this.textureManager = new TextureManager(this, spriteBatch);

                this.startupProfiler.actionEnd("Load Textures");

                // Initializing entity renderer
                this.entityRenderer = new EntityRenderer();
                return null;
            }));

            this.blockRendererRegistry.init(); // must be called before setup of the texture map
            this.textureManager.blockTextureMap.setupTextureMap();

            this.gameSettings = new GameSettings(this);

            // Initializing Audio
            SoundResource.initSounds();

            // Initializing music ticker
            this.musicTicker = new MusicTicker(this);

            glContext.runOnGLContext(() -> textureManager.blockTextureMap.build());

            this.ingameHud = new IngameHud(spriteBatch, this);

            glContext.runOnGLContextWait(new FutureTask<Void>(() -> {
                this.thePlayer = new EntityPlayerSP(null, this, "Steve", new ClientSkin(Gdx.files.classpath("textures/entities/steve.png")));
                return null;
            }));

            this.inputHandler.registerInputProcessor(new GameInput());

            this.startupProfiler.actionEnd();
            this.startupProfiler.printResults();
            this.loaded = true;

            glContext.runOnGLContext(() -> System.out.println("Renderer: " + Gdx.gl.glGetString(GL20.GL_RENDERER)));

            this.uiManager.displayGuiScreen(new GuiMainMenu());
        } catch (Throwable e) {
            this.getLogger().crash("Fatal crash!!! Failed to start Minecraft!", e);
        }
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
        DPI.update();
        if (thePlayer != null) {
            thePlayer.camera.viewportWidth = width;
            thePlayer.camera.viewportHeight = height;
        }
        if (spriteBatch != null)
            this.spriteBatch.getProjectionMatrix().setToOrtho(0, DPI.scaledWidthi, DPI.scaledHeighti, 0, 0, 1);

        if (shapeRenderer != null) {
            this.shapeRenderer.getProjectionMatrix().setToOrtho(0, DPI.scaledWidthi, DPI.scaledHeighti, 0, 0, 1);
            this.shapeRenderer.updateMatrices();
        }
        if (uiManager != null)
            this.uiManager.resize(DPI.scaledWidthi, DPI.scaledHeighti);
    }

    /**
     * Updates the game
     *
     * @param partialTicks the ticks to be performed in the current frame being rendered.
     */
    @Override
    public void tick(float partialTicks) {

        this.musicTicker.tick(partialTicks);
        this.uiManager.tick(partialTicks);
        if (theWorld != null) {
            try {
                theWorld.tick(partialTicks);
            } catch (Throwable t) {
                this.getLogger().crash("Exception on World tick!", t);
            }
        }
    }

    /**
     * Called on OpenGL Context to update timer
     */
    @Override
    @NeedsOpenGLContext
    public void onOpenGLContext(float partialTicks) {
        if (theWorld != null) {
            try {
                this.theWorld.onOpenGLContext(partialTicks);
            } catch (Throwable t) {
                this.getLogger().crash("Failed to update World (OpenGL-Thread)", t);
            }
        }
    }


    @Override
    public void render() {
        if (this.startUpThread == null || this.startUpThread.isAlive() || this.startUpThread.getState() == Thread.State.NEW) {
            if (!drawMojangLogo())
                return;
        }
        if (this.startUpThread == null || this.startUpThread.isAlive())
            return;

        if (!running) {
            close();
            Gdx.app.exit();
        }
        try {
            // Tick
            onUpdate();

//            long frameStart = System.nanoTime();
//            long worldStart = 0, worldEnd = 0;
            //Game Rendering
            {
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);

                Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                if (this.thePlayer != null && this.theWorld != null && thePlayer.getWorld() == theWorld) {
                    float partialTicks = getTimer().partialTicks;

                    shaderManager.stdShader.begin();
                    shaderManager.stdShader.pushMatrix();
                    this.thePlayer.setupViewBobbing(partialTicks);
                    this.thePlayer.updateCameraController(partialTicks);
                    this.thePlayer.updateCamera();

                    shaderManager.stdShader.enableLighting();

                    this.shaderManager.stdShader.renderStart();

//                    worldStart = System.nanoTime();
                    this.theWorld.render(partialTicks);
//                    worldEnd = System.nanoTime();

                    this.thePlayer.rayTracer.update();

                    this.thePlayer.renderCamControllerOverlay();

                    shaderManager.stdShader.renderEnd();
                    shaderManager.stdShader.end();
                    shaderManager.stdShader.popMatrix();
                }

                Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
                Gdx.gl.glDisable(GL20.GL_CULL_FACE);
                Gdx.gl.glDepthMask(false);

                // 2D Rendering starts here

                if (thePlayer != null && theWorld != null)
                    this.ingameHud.render();
                this.uiManager.render();
            }

            //TODO: REMOVE
            //Updating timer and ticks
            {
                openGLOperationsTimer.advanceTime();
                for (int i = 0; i < openGLOperationsTimer.ticks; i++) {
                    onOpenGLContext(openGLOperationsTimer.partialTicks);
                }
            }

//            long frameEnd = System.nanoTime();

//            double renderDif = (frameEnd - frameStart) / 1E6;
//            double worldDif = (worldEnd - worldStart) / 1E6;
//            if (renderDif > 5)
//                System.err.println("renderFrame Took: " + (renderDif) + " ms; renderWorld: " + worldDif);

            int error;
            if ((error = Gdx.gl.glGetError()) != GL_NO_ERROR) {
                System.err.println("OpenGL error: " + OpenGLUtils.getGLErrorName(error));
            }
        } catch (Throwable t) {
            this.getLogger().crash("Failed to render game!", t);
        }
    }

    /**
     * State whether the mojang logo was already rendered before
     */
    private boolean mojangLogoRendered = false;

    /**
     * Mojang logo for startup splash screen
     */
    private CustomTexture mojangTexture;

    /**
     * Draws the mojang logo
     *
     * @return true, if the logo was already rendered before
     */
    private boolean drawMojangLogo() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (mojangLogoRendered && spriteBatch == null) {
            this.spriteBatch = new SpriteBatch();
            this.spriteBatch.getProjectionMatrix().setToOrtho(0, DPI.scaledWidthi, DPI.scaledHeighti, 0, 0, 1);
        }
        if (mojangLogoRendered && mojangTexture == null) {
            mojangTexture = new CustomTexture(Gdx.files.classpath("textures/gui/mojang.png"));
        }

        if (mojangTexture != null) {
            // Drawing the mojang logo
            spriteBatch.begin();
            float size = min(DPI.scaledWidthi, DPI.scaledHeighti);
            spriteBatch.draw(mojangTexture,
                    DPI.scaledWidthi / 2f - size / 2f, DPI.scaledHeighti / 2f - size / 2f,
                    size, size,
                    0, 0, 1, 1
            );
            spriteBatch.end();
        }

        boolean state = mojangLogoRendered;
        mojangLogoRendered = true;
        return state;
    }

    @Override
    public void shutdown() {
        if (theWorld != null)
            this.theWorld.stopAsyncWork();
        running = false;
    }

    /**
     * Called to close the game.
     * Implemented by the Application Launcher
     */
    protected abstract void close();

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

        resumeProfiler.actionEnd("Recreating textures");
        theWorld.invalidateMeshes();
        resumeProfiler.actionEnd();
        resumeProfiler.printResults();
    }

    /**
     * Called on game termination
     */
    private void onGameClosed() {
        if (nettyClient != null)
            this.nettyClient.interrupt();
//        /* Test Only */
//        LevelSaver levelSaver = new LevelSaver(theWorld);
//        try {
//            new File("saves").mkdirs();
//            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//            levelSaver.save(new DataOutputStream(byteOut));
//            new FileOutputStream("saves/test_save.lvl").serialize(byteOut.toByteArray());
//        } catch (IOException e) {
//            throw new RuntimeException("Saving of level failed", e);
//        }
        /* Test Only End */
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

    /**
     * @return true if Minecraft is running (meaning the rendering thread is alive [only for Client Minecraft])
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Shuts down and closes the netty client and nullifies the world instance
     */
    public void closeServerConnection() {
        if (nettyClient != null) {
            nettyClient.interrupt();
            nettyClient = null;
        }
        if (theWorld != null) {
            theWorld.stopAsyncWork();
            theWorld = null;
        }
    }

    @Override
    public boolean isCallingFromMinecraftThread() {
        return GLContext.getGlContext().isCallingFromOpenGLThread();
    }
}