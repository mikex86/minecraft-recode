package me.gommeantilegit.minecraft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.annotations.AndroidOnly;
import me.gommeantilegit.minecraft.annotations.NeedsOpenGLContext;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.ClientBlock;
import me.gommeantilegit.minecraft.block.ClientBlocks;
import me.gommeantilegit.minecraft.block.state.ClientBlockState;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerSP;
import me.gommeantilegit.minecraft.entity.player.skin.ClientSkin;
import me.gommeantilegit.minecraft.entity.renderer.EntityRenderer;
import me.gommeantilegit.minecraft.gamesettings.GameSettings;
import me.gommeantilegit.minecraft.hud.IngameHud;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.input.GameInput;
import me.gommeantilegit.minecraft.input.InputHandler;
import me.gommeantilegit.minecraft.netty.NettyClient;
import me.gommeantilegit.minecraft.profiler.Profiler;
import me.gommeantilegit.minecraft.shader.ShaderManager;
import me.gommeantilegit.minecraft.sound.SoundEngine;
import me.gommeantilegit.minecraft.sound.SoundResource;
import me.gommeantilegit.minecraft.texture.custom.CustomTexture;
import me.gommeantilegit.minecraft.texture.manager.TextureManager;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.timer.api.OpenGLOperation;
import me.gommeantilegit.minecraft.timer.tick.MinecraftThread;
import me.gommeantilegit.minecraft.ui.UIManager;
import me.gommeantilegit.minecraft.ui.screen.impl.GuiMainMenu;
import me.gommeantilegit.minecraft.world.ClientWorld;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static java.lang.Integer.min;

@SideOnly(side = Side.CLIENT)
public abstract class ClientMinecraft extends AbstractMinecraft<ClientBlock, ClientMinecraft, ClientBlocks, ClientBlockState> implements ApplicationListener, OpenGLOperation {

    /**
     * Shader manager instance
     */
    public ShaderManager shaderManager;

    /**
     * Handler object for handling input listeners.
     */
    public InputHandler inputHandler;

    /**
     * Direction of ambient light
     */
    @NotNull
    public Vector3 ambientLightDirection = new Vector3(1, 5, 5).nor();

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
     * The 3D Sound engine instance
     */
    public SoundEngine soundEngine;

    /**
     * Manger object for handling GuiScreens
     */
    public UIManager uiManager;

    /**
     * State whether the game is running or not
     */
    private boolean running = true;

    /**
     * State whether tasks are scheduled on the OpenGL thread or not
     */
    private boolean tasksToRun = false;

    /**
     * {@link GameSettings} instance
     */
    public GameSettings gameSettings;

    /**
     * ShapeRenderer instance for 2D rendering
     */
    public ShapeRenderer shapeRenderer;

    /**
     * Queue of runnables to be executed on the OpenGL thread
     */
    @NotNull
    private final Queue<FutureTask> runnables = new LinkedList<>();

    public ClientMinecraft() {
        super(Side.CLIENT, ClientBlockState.class);
        renderingThread = Thread.currentThread();
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
                startUpThread.start();
            } catch (Throwable t) {
                this.logger.crash("Failed to load game!", t);
            }
        else try {
            resumeAndroid();
        } catch (Throwable t) {
            this.logger.crash("Failed to resume game (Android)", t);
        }

        this.logger.info("Created game.");
    }

    @NotNull
    @Override
    protected MinecraftThread createMinecraftThread() {
        return new MinecraftThread(this);
    }

    /**
     * Loads the game.
     */
    protected void loadGame() {
        this.startupProfiler = new Profiler("Game-startup", false); // Initializing start-up profiler

        super.loadGame();
        Runtime.getRuntime().addShutdownHook(new Thread(this::onGameClosed));

        this.inputHandler = new InputHandler(); // Initializing input handler
        Gdx.input.setInputProcessor(inputHandler);

        // Initializing sprite batch for 2D rendering
        runOnGLContextWait(new FutureTask<Void>(() -> {
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

        runOnGLContextWait(new FutureTask<Void>(() -> {
            this.startupProfiler.actionStart();
            this.startupProfiler.actionStart("Compile shaders");
            this.shaderManager = new ShaderManager();
            this.shaderManager.compileShaders();
            this.startupProfiler.actionEnd("Compile shaders");
            return null;
        }));

        runOnGLContextWait(new FutureTask<Void>(() -> {
            // Initializing UiManager for GuiScreen handling
            this.uiManager = new UIManager(spriteBatch, this);
            return null;
        }));

        runOnGLContextWait(new FutureTask<Void>(() -> {
            this.startupProfiler.actionStart("Load Textures");

            //Initializing textures
            this.textureManager = new TextureManager(this, spriteBatch);

            this.startupProfiler.actionEnd("Load Textures");

            // Initializing entity renderer
            this.entityRenderer = new EntityRenderer();
            return null;
        }));

        this.gameSettings = new GameSettings(this);

        // Initializing Audio
        this.soundEngine = new SoundEngine();
        SoundResource.init();

        this.blocks = new ClientBlocks(this);

        // Initializing Blocks - Must be called after texture manger initialization -> access to textureManager -> building texture atlas
        this.blocks.init();

        runOnGLContext(new FutureTask<Void>(() -> {
            this.blocks.buildTextureMap();
            return null;
        }));

        this.ingameHud = new IngameHud(spriteBatch, this);

        runOnGLContextWait(new FutureTask<Void>(() -> {
            this.thePlayer = new EntityPlayerSP(null, this, "Steve", new ClientSkin(Gdx.files.classpath("textures/entities/steve.png")));
            return null;
        }));

        this.inputHandler.registerInputProcessor(new GameInput());

        this.startupProfiler.actionEnd();
        this.startupProfiler.printResults();
        this.loaded = true;

        runOnGLContext(new FutureTask<Void>(() -> {
            System.out.println("Renderer: " + Gdx.gl.glGetString(GL20.GL_RENDERER));
            return null;
        }));

        this.uiManager.displayGuiScreen(new GuiMainMenu());

        //Initializing muttiplayer netty client
//        this.nettyClient = new NettyClient("localhost", 25565, this);

//        this.nettyClient.start();
//
        this.minecraftThread.start(); //Starting Tick Thread.
//
//        while (!nettyClient.netHandlerPlayClient.isSessionEstablished()) { //TODO: LOADING SCREEN AND STUFF
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        while (!nettyClient.netHandlerPlayClient.isWorldSetup()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
        this.minecraftThread.startMinecraftGameLogic();
    }

    /**
     * Executes the specified runnable on the OpenGL Thrad an waits until it was executed
     *
     * @param voidFutureTask the future task to be executed
     */
    public void runOnGLContextWait(@NotNull FutureTask voidFutureTask) {
        runOnGLContext(voidFutureTask);
        try {
            voidFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the specified runnable on the OpenGL Thread
     *
     * @param runnable the runnable to be executed
     */
    public void runOnGLContext(@NotNull FutureTask runnable) {
        synchronized (this.runnables) {
            this.runnables.add(runnable);
        }
        this.tasksToRun = true;
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
        this.uiManager.tick(partialTicks);
        if (theWorld != null) {
            try {
                theWorld.tick(partialTicks);
            } catch (Throwable t) {
                this.logger.crash("Exception on World tick!", t);
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
                this.logger.crash("Failed to update World (OpenGL-Thread)", t);
            }
        }
    }


    @Override
    public void render() {
        if (this.startUpThread == null || this.startUpThread.isAlive() || this.startUpThread.getState() == Thread.State.NEW) {
            if (!drawMojangLogo())
                return;
        }

        if (tasksToRun) {
            synchronized (runnables) {
                if (!runnables.isEmpty()) {
                    runnables.remove().run();
                }
            }
            tasksToRun = !runnables.isEmpty();
        }
        if (this.startUpThread == null || this.startUpThread.isAlive())
            return;

        if (!running) {
            close();
            Gdx.app.exit();
        }
        try {
            //Game Rendering
            {

                Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);

                Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                if (this.thePlayer != null && this.theWorld != null && thePlayer.getWorld() == theWorld) {
                    float partialTicks = timer.partialTicks;

                    this.thePlayer.updateCameraController(partialTicks);
                    this.thePlayer.updateCamera();

                    shaderManager.stdShader.begin();
                    shaderManager.stdShader.enableLighting();

                    this.shaderManager.stdShader.renderStart();

                    this.theWorld.render(partialTicks);

                    this.thePlayer.rayTracer.update();

                    this.thePlayer.renderCamControllerOverlay();

                    shaderManager.stdShader.renderEnd();
                    shaderManager.stdShader.end();
                }

                Gdx.gl.glDisable(GL20.GL_CULL_FACE);
                Gdx.gl.glDepthMask(false);

                // 2D Rendering starts here

                if (thePlayer != null && theWorld != null)
                    this.ingameHud.render();
                this.uiManager.render();
            }

            //Updating timer and ticks
            {
                openGLOperationsTimer.advanceTime();
                for (int i = 0; i < openGLOperationsTimer.ticks; i++) {
                    onOpenGLContext(openGLOperationsTimer.partialTicks);
                }
            }
        } catch (Throwable t) {
            this.logger.crash("Failed to render game!", t);
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
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);

        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
        this.minecraftThread.interrupt();
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
     * @return true, if this method was executed on the OpenGL thread else false
     */
    public boolean isCallingFromOpenGLThread() {
        return Thread.currentThread() == this.renderingThread;
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
}