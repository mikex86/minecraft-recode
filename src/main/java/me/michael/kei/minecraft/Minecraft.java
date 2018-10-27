package me.michael.kei.minecraft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Vector3;
import me.michael.kei.minecraft.block.Block;
import me.michael.kei.minecraft.block.Blocks;
import me.michael.kei.minecraft.shader.ShaderManager;
import me.michael.kei.minecraft.world.World;
import me.michael.kei.minecraft.world.renderer.WorldRenderer;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class Minecraft implements ApplicationListener {

    public static Minecraft mc;
    public final ShaderManager shaderManager = new ShaderManager();
    public final VertexAttributes vertexAttributes = new VertexAttributes(

            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_Color"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_Normal"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_TextureCoord")
    );
    private Camera camera;
    private CamController camController;
    private Vector3 ambientLightDirection = new Vector3(10, 10, 10).nor();
    private World theWorld;
    private WorldRenderer worldRenderer;

    public Minecraft() {
        mc = this;
    }

    @Override
    public void create() {
        loadGame();
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        camera.position.set(10, 10, 10);

        camera.near = 0.1f;
        camera.far = 170f;
        camera.update();
        Gdx.input.setCursorCatched(true);
        camController = new CamController(camera, 0.2f);
        Gdx.input.setInputProcessor(camController);
    }

    private void loadGame() {
        shaderManager.compileShaders();
        theWorld = new World(128, 64, 128);
        {
            //TestOnly - Generating pane
            for(int x = 0; x < theWorld.getWidth(); x++){
                for(int y = 0; y < 10; y++) {
                    for (int z = 0; z < theWorld.getDepth(); z++) {
                        theWorld.getBlocks()[x][y][z] = Blocks.STONE.getId();
                    }
                }
                for(int y = 10; y < 15; y++){
                    for (int z = 0; z < theWorld.getDepth(); z++) {
                        theWorld.getBlocks()[x][y][z] = Blocks.DIRT.getId();
                    }
                }
            }
        }
        worldRenderer = new WorldRenderer(theWorld);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportHeight = height;
        camera.viewportWidth = width;
    }

    @Override
    public void render() {

        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update(true);
        camController.update(Gdx.graphics.getDeltaTime());

        shaderManager.stdShader.begin();
        shaderManager.stdShader.setUniformMatrix("matViewProj", camera.combined);
        shaderManager.stdShader.setUniformf("lightDirection", ambientLightDirection);
        worldRenderer.render();
        shaderManager.stdShader.end();

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

    private class CamController extends InputAdapter {

        private final Camera cam;
        private final float mouseSensitivity;
        private final float velocity = 3f;
        private final Vector3 tmp = new Vector3();
        private HashMap<Integer, Boolean> keys = new HashMap<>();
        private int STRAFE_LEFT = Input.Keys.A;
        private int STRAFE_RIGHT = Input.Keys.D;
        private int FORWARD = Input.Keys.W;
        private int BACKWARD = Input.Keys.S;
        private int UP = Input.Keys.Q;
        private int DOWN = Input.Keys.E;
        public CamController(Camera camera, float mouseSensitivity) {
            this.cam = camera;
            this.mouseSensitivity = mouseSensitivity;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            Gdx.input.setCursorCatched(true);
            float deltaX = -Gdx.input.getDeltaX() * mouseSensitivity;
            float deltaY = -Gdx.input.getDeltaY() * mouseSensitivity;
            cam.direction.y += deltaY * 0.05;
            cam.rotate(deltaX, 0, 1, 0);
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            keys.put(keycode, true);
            return super.keyDown(keycode);
        }

        @Override
        public boolean keyUp(int keycode) {
            keys.put(keycode, false);
            return super.keyUp(keycode);
        }

        public void update(float deltaTime) {
            if (key(FORWARD)) {
                tmp.set(camera.direction).nor().scl(deltaTime * velocity);
                camera.position.add(tmp);
            }
            if (key(BACKWARD)) {
                tmp.set(camera.direction).nor().scl(-deltaTime * velocity);
                camera.position.add(tmp);
            }
            if (key(STRAFE_LEFT)) {
                tmp.set(camera.direction).crs(camera.up).nor().scl(-deltaTime * velocity);
                camera.position.add(tmp);
            }
            if (key(STRAFE_RIGHT)) {
                tmp.set(camera.direction).crs(camera.up).nor().scl(deltaTime * velocity);
                camera.position.add(tmp);
            }
            if (key(UP)) {
                tmp.set(camera.up).nor().scl(deltaTime * velocity);
                camera.position.add(tmp);
            }
            if (key(DOWN)) {
                tmp.set(camera.up).nor().scl(-deltaTime * velocity);
                camera.position.add(tmp);
            }
        }

        private boolean key(int key) {
            return keys.get(key) != null && keys.get(key);
        }
    }
}