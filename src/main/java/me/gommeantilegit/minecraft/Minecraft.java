package me.gommeantilegit.minecraft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import me.gommeantilegit.minecraft.entity.player.Player;
import me.gommeantilegit.minecraft.hud.IngameHud;
import me.gommeantilegit.minecraft.input.InputHandler;
import me.gommeantilegit.minecraft.shader.ShaderManager;
import me.gommeantilegit.minecraft.timer.Timer;
import me.gommeantilegit.minecraft.world.renderer.WorldRenderer;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

public class Minecraft implements ApplicationListener {

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

    @NotNull
    public final InputHandler inputHandler = new InputHandler();

    //    private Vector3 ambientLightDirection = new Vector3(10, 10, 10).nor();
    private World theWorld;
    private WorldRenderer worldRenderer;
    public int width, height;
    private Player thePlayer;
    private final Thread renderingThread;
    private IngameHud ingameHud;
    public Timer timer = new Timer(20.0f);

    public Minecraft() {
        mc = this;
        renderingThread = Thread.currentThread();
    }

    @Override
    public void create() {
        loadGame();
        Gdx.input.setCursorCatched(true);
        Gdx.input.setInputProcessor(inputHandler);
    }

    private void loadGame() {
        shaderManager.compileShaders();
        theWorld = new World(128, 64, 128);
        genTestWorld();
        this.ingameHud = new IngameHud();
        worldRenderer = new WorldRenderer(theWorld);
        thePlayer = new Player(theWorld);
        this.inputHandler.registerInputProcessor(Player.CAM_CONTROLLER);
        theWorld.spawnEntityInWorld(thePlayer);
    }

    @TestOnly
    private void genTestWorld() {
        //TestOnly - Generating pane
        for (int x = 0; x < theWorld.getWidth(); x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < theWorld.getDepth(); z++) {
                    theWorld.getBlocks()[x][y][z] = Blocks.STONE.getId();
                }
            }
            for (int y = 10; y < 15; y++) {
                for (int z = 0; z < theWorld.getDepth(); z++) {
                    theWorld.getBlocks()[x][y][z] = Blocks.DIRT.getId();
                }
            }
            int y = 15;
            for (int z = 0; z < theWorld.getDepth(); z++) {
                theWorld.getBlocks()[x][y][z] = Blocks.GRASS.getId();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        Player.updateViewportSize(width, height);
    }

    @Override
    public void render() {
        //Updating timer and ticks
        {
            this.timer.advanceTime();
            for (int i = 0; i < timer.ticks; i++)
                this.tick();
        }
        //Game Rendering
        {
            Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
            Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            Player.CAMERA.update(true);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shaderManager.stdShader.begin();
            shaderManager.stdShader.setUniformMatrix("matViewProj", Player.CAMERA.combined);
            worldRenderer.render(timer.partialTicks);
            shaderManager.stdShader.end();
            this.thePlayer.updateCamera();
            this.ingameHud.render();
        }
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

    private void tick() {
        theWorld.tick();
    }
}