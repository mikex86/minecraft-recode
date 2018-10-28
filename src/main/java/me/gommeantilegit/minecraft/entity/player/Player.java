package me.gommeantilegit.minecraft.entity.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.Minecraft;
import me.gommeantilegit.minecraft.entity.Entity;
import me.gommeantilegit.minecraft.entity.player.model.PlayerModel;
import me.gommeantilegit.minecraft.world.World;

import java.util.HashMap;

public class Player extends Entity {

    public final static Camera CAMERA = new PerspectiveCamera(100, Minecraft.mc.width, Minecraft.mc.height);
    public final static CamController CAM_CONTROLLER = new CamController(0.2f);
    private static final Texture PLAYER_TEXTURE = new Texture("textures/entities/steve.png");
    private static final PlayerModel PLAYER_MODEL = new PlayerModel(PLAYER_TEXTURE);

    static {
        CAMERA.near = 0.1f;
        CAMERA.far = 170f;
        CAMERA.update();
    }

    public Player(World world) {
        super(world, 20);
    }

    @Override
    public void tick() {
//        this.rotationYaw = (float) Math.toDegrees(CAMERA.direction.x);
//        this.rotationPitch = (float) (-(float) Math.toDegrees(CAMERA.direction.y) / Math.PI);
    }

    public void updateCamera(){
        CAM_CONTROLLER.update(Gdx.graphics.getDeltaTime());
    }

    public static void updateViewportSize(int width, int height) {
        CAMERA.viewportWidth = width;
        CAMERA.viewportHeight = height;
    }

    private static class CamController extends InputAdapter {

        private final float mouseSensitivity;
        private final float velocity = 10f;

        private final Vector3 tmp = new Vector3();
        private HashMap<Integer, Boolean> keys = new HashMap<>();
        private int STRAFE_LEFT = Input.Keys.A;
        private int STRAFE_RIGHT = Input.Keys.D;
        private int FORWARD = Input.Keys.W;
        private int BACKWARD = Input.Keys.S;
        private int UP = Input.Keys.Q;
        private int DOWN = Input.Keys.E;
        private int oldX, oldY;

        public CamController(float mouseSensitivity) {
            this.mouseSensitivity = mouseSensitivity;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            Gdx.input.setCursorCatched(true);
            float deltaX = (oldX - screenX) * mouseSensitivity;
            float deltaY = (oldY - screenY) * mouseSensitivity;
            oldX = screenX;
            oldY = screenY;
            CAMERA.direction.y += deltaY * 0.05;
            CAMERA.rotate(deltaX, 0, 1, 0);
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
                tmp.set(CAMERA.direction).nor().scl(deltaTime * velocity);
                CAMERA.position.add(tmp);
            }
            if (key(BACKWARD)) {
                tmp.set(CAMERA.direction).nor().scl(-deltaTime * velocity);
                CAMERA.position.add(tmp);
            }
            if (key(STRAFE_LEFT)) {
                tmp.set(CAMERA.direction).crs(CAMERA.up).nor().scl(-deltaTime * velocity);
                CAMERA.position.add(tmp);
            }
            if (key(STRAFE_RIGHT)) {
                tmp.set(CAMERA.direction).crs(CAMERA.up).nor().scl(deltaTime * velocity);
                CAMERA.position.add(tmp);
            }
            if (key(UP)) {
                tmp.set(CAMERA.up).nor().scl(deltaTime * velocity);
                CAMERA.position.add(tmp);
            }
            if (key(DOWN)) {
                tmp.set(CAMERA.up).nor().scl(-deltaTime * velocity);
                CAMERA.position.add(tmp);
            }
        }

        private boolean key(int key) {
            return keys.get(key) != null && keys.get(key);
        }
    }

    @Override
    public void render(float partialTicks) {

        Matrix4 copy = CAMERA.combined.cpy();
//        CAMERA.combined.translate((lastPosX + (posX - lastPosX) * partialTicks), (lastPosY + (posY - lastPosY) * partialTicks), (lastPosZ + (posZ - lastPosZ) * partialTicks));
//        CAMERA.combined.rotate(rotationYaw, 0.0f, 1.0f, 0.0f);
        ShaderProgram shader = Minecraft.mc.shaderManager.stdShader;
        shader.setUniformMatrix("matViewProj", CAMERA.combined);
        PLAYER_MODEL.render(partialTicks, this, shader, CAMERA);
        CAMERA.combined.set(copy);

    }
}
