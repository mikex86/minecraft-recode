package me.gommeantilegit.minecraft.entity.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.BlockBase;
import me.gommeantilegit.minecraft.entity.player.base.PlayerBase;
import me.gommeantilegit.minecraft.entity.player.packet.PacketSender;
import me.gommeantilegit.minecraft.entity.player.skin.ClientSkin;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.raytrace.RenderingRayTracer;
import me.gommeantilegit.minecraft.texture.TextureWrapper;
import me.gommeantilegit.minecraft.ui.button.Button;
import me.gommeantilegit.minecraft.ui.button.TexturedButton;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;
import me.gommeantilegit.minecraft.ui.screen.impl.GuiIngamePause;
import me.gommeantilegit.minecraft.ui.screen.impl.GuiOptions;
import me.gommeantilegit.minecraft.util.block.facing.EnumFacing;
import me.gommeantilegit.minecraft.util.block.position.BlockPos;
import me.gommeantilegit.minecraft.utils.Clock;
import me.gommeantilegit.minecraft.world.ClientWorld;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import me.gommeantilegit.minecraft.world.chunk.ClientChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.FutureTask;

import static java.lang.Math.hypot;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

@SideOnly(side = Side.CLIENT)
public class EntityPlayerSP extends PlayerBase<ClientSkin> {

    /*
     * MOVE KEY CONSTANTS
     * //TODO: GAME-SETTINGS SYSTEM
     */
    private static final int FORWARDS = Input.Keys.W;
    private static final int BACKWARDS = Input.Keys.S;
    private static final int LEFT = Input.Keys.A;
    private static final int RIGHT = Input.Keys.D;
    private static final int JUMP = Input.Keys.SPACE;
    private static final int SNEAKING = Input.Keys.SHIFT_LEFT;
    private static final int SPRINTING = Input.Keys.CONTROL_LEFT;
    private static final int SWITCH_CAMERA_MODE = Input.Keys.F5;

    /**
     * Controller object for player logic such as block breaking
     */
    @NotNull
    public final PlayerController playerController;

    @NotNull
    public final ClientMinecraft mc;

    /**
     * Object that sends packets to the server about the player's state
     */
    @NotNull
    private final PacketSender packetSender = new PacketSender(this);

    /**
     * State whether the player has spawned in the world
     */
    public boolean spawned = false;

    /**
     * Enum representing all camera modes
     */
    public enum CameraMode {
        FIRST_PERSON, THIRD_PERSON;

        public EntityPlayerSP.CameraMode next() {
            return values()[(Arrays.asList(values()).indexOf(this) + 1) % values().length];
        }
    }

    /**
     * Camera mode
     */
    @NotNull
    private EntityPlayerSP.CameraMode cameraMode = EntityPlayerSP.CameraMode.FIRST_PERSON;

    /**
     * Ray-tracing utility.
     */
    public final RayTracer rayTracer;

    /**
     * Virtual Camera position
     */
    @NotNull
    private final Vector3 cameraPosition = new Vector3();

    /**
     * State whether the player has already received a PositionSetPacket
     */
    private boolean posPacketReceieved = false;

    /**
     * Player perspective camera operating with position zero.
     * Does not update the frustum as it would not match the coordinate space
     */
    @NotNull
    public final PerspectiveCamera camera = new PerspectiveCamera(100, 0, 0) {

        @Override
        public void update() {
            float aspect = viewportWidth / viewportHeight;

            projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);

            view.setToLookAt(position, new Vector3(position).add(direction), new Vector3(0, 1f, 0));
            combined.set(projection);
            Matrix4.mul(combined.val, view.val);
        }

    };

    /**
     * Virtual camera operating with the virtual camera position.
     * Used when the correct camera frustum is needed.
     */
    @NotNull
    public final PerspectiveCamera virtualCamera = new PerspectiveCamera(100, 0, 0) {
        @Override
        public void update() {
            this.direction.set(camera.direction);
            this.position.set(cameraPosition);

            float aspect = viewportWidth / viewportHeight;
            projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
            view.setToLookAt(position, new Vector3(position).add(direction), up);
            combined.set(projection);
            Matrix4.mul(combined.val, view.val);

            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    };

    /**
     * Input processor for controlling the player
     */
    @NotNull
    public final EntityPlayerSP.PlayerInputAdapter camController = getCamController();

    public EntityPlayerSP(@Nullable ClientWorld world, @NotNull ClientMinecraft mc, @NotNull String username, @NotNull ClientSkin skin) {
        super(world, 20, username, skin);
        this.mc = mc;

        rayTracer = new RenderingRayTracer(this);

        camera.viewportWidth = mc.width;
        camera.viewportHeight = mc.height;

        camera.near = 0.1f;
        camera.far = 512f;
        camera.update();

        playerController = new PlayerController(this);
        if (mc.isCallingFromOpenGLThread())
            mc.shaderManager.stdShader.setCamera(camera);
        else
            mc.runOnGLContext(new FutureTask<Void>(() -> {
                mc.shaderManager.stdShader.setCamera(camera);
                return null;
            }));
    }

    public void setCameraMode(@NotNull EntityPlayerSP.CameraMode cameraMode) {
        this.cameraMode = cameraMode;
    }

    /**
     * Updating the player
     */
    @Override
    public void tick() {
        updateMovementValues();
        if (!posPacketReceieved || currentChunk instanceof ClientChunk && !((ClientChunk) currentChunk).dataReceived) {
            mc.thePlayer.motionY *= 0;
        }
        if (!spawned) {
            if (posPacketReceieved) {
                if (currentChunk instanceof ClientChunk && ((ClientChunk) currentChunk).dataReceived) {
                    spawned = true;
                }
            }
        }
        if (posPacketReceieved) {
            super.tick();
            playerController.onPlayerBlockDamage();
            this.packetSender.sendMovePackets();
        }
    }

    @Override
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
        posPacketReceieved = true;
    }

    /**
     * Updates {@link #moveForward} and {@link #moveStrafing} accordingly to the pressed keys
     */
    private void updateMovementValues() {
        boolean forwards = keyDown(FORWARDS);
        boolean backwards = keyDown(BACKWARDS);
        boolean left = keyDown(LEFT);
        boolean right = keyDown(RIGHT);
        boolean jump = keyDown(JUMP);
        boolean sprint = keyDown(SPRINTING);
        boolean sneak = keyDown(SNEAKING);

        if (forwards ^ backwards) {
            this.moveForward = forwards ? 1 : -1;
        } else {
            this.moveForward = 0;
        }
        if (left ^ right) {
            moveStrafing = left ? 1 : -1;
        } else {
            moveStrafing = 0;
        }

        float f = 0.8f;
        if (!this.isSprinting() && this.moveForward >= f && sprint) {
            this.setSprinting(true);
            this.camController.setKeyDown(SPRINTING, false);
        }

        if (this.isSprinting() && (this.moveForward < f || this.collidedHorizontally)) {
            this.setSprinting(false);
        }

        this.setSneaking(sneak);

        setJumping(jump);

        if (keyDown(SWITCH_CAMERA_MODE))
            this.cameraMode = cameraMode.next();
    }

    @NotNull
    private EntityPlayerSP.PlayerInputAdapter getCamController() {
        switch (Gdx.app.getType()) {
            case Android:
            case iOS:
                return new EntityPlayerSP.MobilePlayerController(0.7f, this, mc);
            case Desktop:
                return new EntityPlayerSP.DesktopPlayerController(0.7f, this);
            default:
                throw new IllegalStateException("Unsupported platform: " + Gdx.app.getType());
        }
    }

    /**
     * @param key the given key code
     * @return the state of the key currently being pressed.
     */
    private boolean keyDown(int key) {
        return camController.key(key);
    }

    /**
     * Updates the camera's position according to the players position
     *
     * @param partialTicks partialTicks
     */
    private void setCamPos(float partialTicks) {

        this.entityRenderPosition.set(
                lastPosX, lastPosY, lastPosZ,
                posX, posY, posZ);

        Vector3 pos = new Vector3(
                entityRenderPosition.lastPosX + (entityRenderPosition.posX - entityRenderPosition.lastPosX) * partialTicks,
                entityRenderPosition.lastPosY + (entityRenderPosition.posY - entityRenderPosition.lastPosY) * partialTicks + EYE_HEIGHT,
                entityRenderPosition.lastPosZ + (entityRenderPosition.posZ - entityRenderPosition.lastPosZ) * partialTicks);

        switch (this.cameraMode) {

            case THIRD_PERSON: {
                pos.add(camera.direction.cpy().scl(-3));
                break;
            }

        }
        cameraPosition.set(pos.x, pos.y, pos.z);
        mc.shaderManager.stdShader.setVirtualCameraPos(pos.x, pos.y, pos.z);
        camera.position.set(0, 0, 0);
    }

    /**
     * Doing nothing when entity render position is called
     */
    @Override
    protected void setEntityRenderPosition() {

    }

    public void updateCameraController(float partialTicks) {
        camController.update(Gdx.graphics.getDeltaTime(), partialTicks);
    }

    public void updateCamera() {
        camera.update();
        virtualCamera.update();
        this.mc.shaderManager.stdShader.setCamera(camera);
    }

    public void renderCamControllerOverlay() {
        camController.render();
    }

    /**
     * Called when the player breaks a block
     *
     * @param block           the block broken
     * @param pos             the position of the block broken
     * @param facingDirection rayTraceResult side hit direction.
     * @see me.gommeantilegit.minecraft.raytrace.RayTracer.RayTraceResult#hitSide
     */
    void onBlockBroken(@NotNull BlockBase block, @NotNull BlockPos pos, @NotNull EnumFacing facingDirection) {
        this.breakMouseOver();
    }

    /**
     * API for a player controller input processor
     */
    private static abstract class PlayerInputAdapter extends InputAdapter {


        /**
         * The player to be controlled
         */
        @NotNull
        protected final EntityPlayerSP player;

        /**
         * Keymap with stored key states
         */
        @NotNull
        protected HashMap<Integer, Boolean> keys = new HashMap<>();

        protected float mouseSensitivity;

        protected PlayerInputAdapter(@NotNull EntityPlayerSP player, float mouseSensitivity) {
            this.player = player;
            this.mouseSensitivity = mouseSensitivity;
        }

        /**
         * Updates the input processor
         *
         * @param deltaTime    gdx delta time
         * @param partialTicks timer delta
         */
        abstract void update(float deltaTime, float partialTicks);

        /**
         * Sets the state of the given keycode being held
         *
         * @param keyCode the given keycode
         * @param state   the new state
         */
        void setKeyDown(int keyCode, boolean state) {
            if (state)
                player.onKeyDown(keyCode);
            else player.onKeyUp(keyCode);
            keys.put(keyCode, state);
        }

        /**
         * @param key a given keycode
         * @return if the key is currently being held down
         */
        boolean key(int key) {
            return keys.get(key) != null && keys.get(key);
        }

        /**
         * Renders an overlay of the controller, if needed
         */
        void render() {
        }
    }

    /**
     * Clock instance used to determine, if the player has stopped walking and quickly started again so sprinting can be toggled.
     */
    private final Clock sprintingStartClock = new Clock(false);

    /**
     * Invoked when a key is pressed
     *
     * @param keyCode the key-code that was just pressed down
     */
    private void onKeyDown(int keyCode) {
        if (keyCode == FORWARDS) {
            if (sprintingStartClock.getTimePassed() <= 300) {
                setSprinting(true);
            }
            sprintingStartClock.reset();
        } else if (keyCode == Input.Keys.ESCAPE)
            mc.uiManager.displayGuiScreen(new GuiIngamePause());
    }

    /**
     * Invoked when a key is released.
     *
     * @param keyCode the key-code that was released
     */
    private void onKeyUp(int keyCode) {

    }

    /**
     * Class Wrapper converting mouse and keyboard input into player rotation and movement
     */
    private static class DesktopPlayerController extends EntityPlayerSP.PlayerInputAdapter {

        /**
         * Temp vector for rotation calculation
         */
        @NotNull
        private final Vector3 tmp = new Vector3();

        /**
         * Last frames player pitch in degrees
         */
        private float lastPitchDegrees;

        /**
         * @param mouseSensitivity sets {@link #mouseSensitivity}
         * @param player           sets {@link #player}
         */
        DesktopPlayerController(float mouseSensitivity, @NotNull EntityPlayerSP player) {
            super(player, mouseSensitivity);
        }

        private int lastX = Gdx.input.getX(), lastY = Gdx.input.getY();

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            updateMouseInput(-screenX + lastX, screenY - lastY);
            lastX = screenX;
            lastY = screenY;
            return super.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            updateMouseInput(-screenX + lastX, screenY - lastY);
            lastX = screenX;
            lastY = screenY;
            return super.mouseMoved(screenX, screenY);
        }

        /**
         * Updates mouse input -> converting to player rotation
         */
        private void updateMouseInput(int deltaX, int deltaY) {

            Gdx.input.setCursorCatched(player.mc.uiManager.currentScreen == null);

            PerspectiveCamera camera = this.player.camera;

            //X Rotation
            {
                camera.direction.rotate(camera.up, deltaX);
                tmp.set(camera.direction).crs(camera.up).nor();
                player.rotationYaw += deltaX;
            }
            //Y Rotation
            {

                float newPitchDegrees = player.rotationPitch + deltaY;
                newPitchDegrees = max(-89, min(newPitchDegrees, 89));
                player.rotationPitch = newPitchDegrees;
                camera.direction.rotate(tmp, lastPitchDegrees - newPitchDegrees);
                lastPitchDegrees = newPitchDegrees;
            }
        }

        /**
         * Called on key-press
         * Updates the keymap's key states
         *
         * @param keycode the keycode pressed
         * @return state
         */
        @Override
        public boolean keyDown(int keycode) {
            setKeyDown(keycode, true);
            return super.keyDown(keycode);
        }

        /**
         * Called on key-release
         * Updates the keymap's key states
         *
         * @param keycode the keycode released
         * @return state
         */
        @Override
        public boolean keyUp(int keycode) {
            setKeyDown(keycode, false);
            return super.keyUp(keycode);
        }

        /**
         * Updating the input processor
         *
         * @param deltaTime delta time
         */
        @Override
        void update(float deltaTime, float partialTicks) {
            //Updating camera position
            player.setCamPos(partialTicks);
        }


        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            //Handling block breaking
            if (button == 0) {
                player.updateBlockBreaking(-1);
            }
            return super.touchDown(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == 0) {
                player.onBlockBreakingAbort();
            }
            return super.touchUp(screenX, screenY, pointer, button);
        }
    }

    /**
     * Not allowing the player's chunk to be unloaded.
     *
     * @param chunk the chunk that the entity is in.
     * @return false
     */
    @Override
    public boolean allowChunkUnload(ChunkBase chunk) {
        return false;
    }

    boolean breakingBlocks = false;

    /**
     * Updates the breaking of the selected block
     *
     * @param pointer pointer touching down. -1, if desktop click
     */
    private void updateBlockBreaking(int pointer) {
        if ((pointer == -1 || !(((EntityPlayerSP.MobilePlayerController) this.camController)).pressingButtonWithRotationPointer)) {
            swingItem();
            RayTracer.RayTraceResult result = rayTracer.getRayTraceResult();
            if (result == null) return;
            if (result.type == RayTracer.RayTraceResult.EnumResultType.BLOCK)
                breakingBlocks = true;
        }
    }

    private void onBlockBreakingAbort() {
        breakingBlocks = false;
        playerController.onBlockBreakingAbort();
    }

    private void breakMouseOver() {
        RayTracer.RayTraceResult result = rayTracer.getRayTraceResult();
        assert result != null;
        if (result.type == RayTracer.RayTraceResult.EnumResultType.BLOCK) {

            assert result.getBlockPos() != null;
            int x = result.getBlockPos().getX(), y = result.getBlockPos().getY(), z = result.getBlockPos().getZ();
            int prevBlockID = world.getBlockID(x, y, z);
            world.setBlock(x, y, z, null);
            mc.theWorld.particleEngine.spawnBlockBreakingParticles(x, y, z, prevBlockID);
        }
    }

    /**
     * Class Wrapper converting mobile touch input into player movement and rotation
     */
    private static class MobilePlayerController extends EntityPlayerSP.PlayerInputAdapter {

        /**
         * Temp vector for rotation calculation
         */
        @NotNull
        private final Vector3 tmp = new Vector3();

        /**
         * State if the player preses a button with it's rotation pointer. -> Only possible button = jump button (for now)
         */
        boolean pressingButtonWithRotationPointer;

        /**
         * Last frames player pitch in degrees
         */
        private float lastPitchDegrees;

        /**
         * Last frames position of the finger
         */
        private int lastScreenX, lastScreenY;

        /**
         * Minecraft instance
         */
        private final ClientMinecraft mc;

        /**
         * @param mouseSensitivity sets {@link #mouseSensitivity}
         * @param player           sets {@link #player}
         * @param mc               sets {@link #mc}
         */
        MobilePlayerController(float mouseSensitivity, @NotNull EntityPlayerSP player, ClientMinecraft mc) {
            super(player, mouseSensitivity);
            this.mc = mc;
        }

        /**
         * Input processing function invoked by {@link EntityPlayerSP.MobilePlayerController.ButtonLayout}
         *
         * @param screenX cursor x coord
         * @param screenY cursor y coord
         * @param pointer pointer index
         */
        void onTouchDragged(int screenX, int screenY, int pointer) {

            float deltaX = (lastScreenX - screenX) * mouseSensitivity;
            float deltaY = (lastScreenY - screenY) * mouseSensitivity;

            this.buttonLayout.pointerMovedInLastSecond += hypot(deltaX, deltaY);

            lastScreenX = screenX;
            lastScreenY = screenY;

            PerspectiveCamera camera = this.player.camera;

            //X Rotation
            {
                camera.direction.rotate(camera.up, deltaX);
                tmp.set(camera.direction).crs(camera.up).nor();
                player.rotationYaw += deltaX;
            }
            //Y Rotation
            {

                float newPitchDegrees = player.rotationPitch - deltaY;
                newPitchDegrees = max(-89, min(newPitchDegrees, 89));
                player.rotationPitch = newPitchDegrees;
                camera.direction.rotate(tmp, lastPitchDegrees - newPitchDegrees);
                lastPitchDegrees = newPitchDegrees;
            }
        }

        /**
         * Updating the input processor
         *
         * @param deltaTime delta time
         */
        @Override
        void update(float deltaTime, float partialTicks) {
            //Updating camera position
            player.setCamPos(partialTicks);
            updateBlockBreaking();
        }

        /**
         * Delta time the player has been staring at a point while holding down the finger.
         */
        private float blockBreakDelta = 0;

        /**
         * Timer for resetting the amount the pointer has moved every second
         */
        private final Clock blockBreakingClock = new Clock(false);

        /**
         * State if the player has already broken a block in the current press of the pointer
         */
        private boolean breakingBlocks = false;

        /**
         * Updates the block breaking input
         */
        private void updateBlockBreaking() {
            if (breakingBlocks && blockBreakingClock.getTimePassed() > 1000) {
                this.buttonLayout.pointerMovedInLastSecond = 0;
                blockBreakingClock.reset();
            }
            if (buttonLayout.rotationPointer == -1) {
                blockBreakDelta = 0;
                breakingBlocks = false;
                this.player.onBlockBreakingAbort();
            }
            if (this.buttonLayout.rotationPointer != -1) {
                if (this.buttonLayout.pointerMovedInLastSecond < 25) {
                    blockBreakDelta += Gdx.graphics.getRawDeltaTime();
                    if (blockBreakDelta > 0.45) {
                        this.player.updateBlockBreaking(this.buttonLayout.rotationPointer);
                        breakingBlocks = true;
                    }
                } else if (this.buttonLayout.pointerMovedInLastSecond > 100) {
                    breakingBlocks = false;
                    blockBreakDelta = 0;
                    this.player.onBlockBreakingAbort();
                }
            }
        }

        /**
         * 2D Overlay representing the button layout
         */
        private final class ButtonLayout extends Overlay2D {

            /**
             * The size of a button of the layout.
             */
            public static final int BUTTON_SIZE = 80;

            /**
             * Space between buttons of the layout.
             */
            public static final int PADDING = 5;

            /**
             * Button controlling the forward keyBind.
             */
            private final TexturedButton forwardButton = new TexturedButton(BUTTON_SIZE, BUTTON_SIZE,
                    BUTTON_SIZE + PADDING * 2, 0,
                    mc.textureManager.guiTextures,
                    mc.textureManager.guiTextures.mobileForwardButton
            );

            /**
             * Button controlling the left keyBind.
             */
            private final TexturedButton leftButton = new TexturedButton(BUTTON_SIZE, BUTTON_SIZE,
                    PADDING, 0,
                    mc.textureManager.guiTextures,
                    mc.textureManager.guiTextures.mobileLeftButton
            );
            /**
             * Button controlling the backwards keyBind.
             */
            private final TexturedButton backwardsButton = new TexturedButton(BUTTON_SIZE, BUTTON_SIZE,
                    BUTTON_SIZE + PADDING * 2, 0,
                    mc.textureManager.guiTextures,
                    mc.textureManager.guiTextures.mobileBackwardsButton);

            /**
             * Button controlling the right keyBind.
             */
            private final TexturedButton rightButton = new TexturedButton(BUTTON_SIZE, BUTTON_SIZE,
                    BUTTON_SIZE * 2 + PADDING * 3, 0,
                    mc.textureManager.guiTextures,
                    mc.textureManager.guiTextures.mobileRightButton);
            /**
             * Button controlling the jump keyBind.
             */
            private final TexturedButton jumpButton = new TexturedButton(BUTTON_SIZE, BUTTON_SIZE,
                    0, 0,
                    mc.textureManager.guiTextures,
                    mc.textureManager.guiTextures.mobileJumpButton);

            /**
             * Array storing all buttons of the layout
             */
            private final Button[] buttons;

            /**
             * Array storing all buttons controlling keyBinds corresponding to a given move direction. eg. left, right backwards forwards.
             */
            private final Button[] moveButtons;

            ButtonLayout() {
                super(mc.spriteBatch);
                this.buttons = new Button[]{forwardButton, leftButton, backwardsButton, rightButton, jumpButton};
                this.moveButtons = new Button[]{forwardButton, leftButton, backwardsButton, rightButton};
                {
                    applyKeyBind(forwardButton, FORWARDS);
                    applyKeyBind(leftButton, LEFT);
                    applyKeyBind(backwardsButton, BACKWARDS);
                    applyKeyBind(rightButton, RIGHT);
                    applyKeyBind(jumpButton, JUMP);
                    //Extra configuration for the jump button
                    {
                        jumpButton.setOnDragEnterListener(pointer -> {
                                    setKeyDown(JUMP, true);
                                    jumpButton.transparency = 0.70;
                                    pressingButtonWithRotationPointer = true;
                                }
                        );
                        jumpButton.setOnDragLeaveListener(pointer -> {
                                    setKeyDown(JUMP, false);
                                    jumpButton.transparency = 0.5;
                                    pressingButtonWithRotationPointer = false;
                                }
                        );
                    }
                }
            }

            /**
             * Configures the button to control a given key-bind.
             *
             * @param button  the button that should handle the given key-bind
             * @param keyBind the specified key to be controlled.
             */
            private void applyKeyBind(TexturedButton button, int keyBind) {
                button.transparency = 0.5;
                button.setOnDragEnterListener(pointer -> {
                            if (pointer != this.rotationPointer) {
                                setKeyDown(keyBind, true);
                                button.transparency = 0.70;
                            }
                        }
                );
                button.setOnDragLeaveListener(pointer -> {
                            setKeyDown(keyBind, false);
                            button.transparency = 0.5;
                        }
                );
            }

            @Override
            public void render() {

                //Move buttons
                {
                    forwardButton.setPosY(DPI.scaledHeighti - BUTTON_SIZE * 3 - PADDING * 2);
                    leftButton.setPosY(DPI.scaledHeighti - BUTTON_SIZE * 2 - PADDING);
                    backwardsButton.setPosY(DPI.scaledHeighti - BUTTON_SIZE);
                    rightButton.setPosY(DPI.scaledHeighti - BUTTON_SIZE * 2 - PADDING);
                }
                {
                    jumpButton.setPosX(DPI.scaledWidthi - BUTTON_SIZE * 2 - PADDING);
                    jumpButton.setPosY(DPI.scaledHeighti - BUTTON_SIZE * 2 - PADDING);
                }

                for (Button button : this.buttons)
                    button.render(Gdx.input.getX(), Gdx.input.getY());

            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {

                if (noMoveKeyHovered(screenX, screenY)) {
                    this.rotationPointer = pointer;
                }

                for (Button guiButton : this.buttons)
                    guiButton.touchDown(screenX, screenY, pointer, button);
                return super.touchDown(screenX, screenY, pointer, button);
            }

            /**
             * @param mouseX pointer x position
             * @param mouseY pointer y position
             * @return true if none of those keys {@link #moveButtons} is being hovered by the given pointer position
             */
            private boolean noMoveKeyHovered(int mouseX, int mouseY) {
                for (Button moveButton : this.moveButtons) {
                    if (moveButton.isHovered(mouseX, mouseY))
                        return false;
                }
                return true;
            }

            /**
             * Pointer id which is actually controlling the player's rotation
             */
            private int rotationPointer = -1;

            /**
             * The amount of pixel the mouse has moved during the last second.
             */
            private int pointerMovedInLastSecond = 0;

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {

                for (Button guiButton : this.buttons)
                    guiButton.touchDragged(screenX, screenY, pointer);

                if (pointer == this.rotationPointer) {
                    onTouchDragged(screenX, screenY, pointer);
                }
                return super.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {

                for (Button guiButton : this.buttons)
                    guiButton.touchUp(screenX, screenY, pointer, button);

                if (pointer == this.rotationPointer) {
                    this.rotationPointer = -1;
                    pointerMovedInLastSecond = 0;
                }
                return super.touchUp(screenX, screenY, pointer, button);
            }
        }

        /**
         * Button layout overlay instance
         */
        private final EntityPlayerSP.MobilePlayerController.ButtonLayout buttonLayout = new EntityPlayerSP.MobilePlayerController.ButtonLayout();

        /**
         * Renders a key overlay
         */
        @Override
        public void render() {
            buttonLayout.render();
        }


        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            buttonLayout.touchDragged(screenX, screenY, pointer);
            return super.touchDragged(screenX, screenY, pointer);
        }


        /**
         * On touch down
         *
         * @param screenX touch down x coord
         * @param screenY touch down y coord
         * @param pointer pointer index
         * @param button  button index
         * @return state
         */
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {


            buttonLayout.touchDown(screenX, screenY, pointer, button);
            if (buttonLayout.rotationPointer == pointer) {
                lastScreenX = screenX;
                lastScreenY = screenY;
            }

            return super.touchDown(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            buttonLayout.touchUp(screenX, screenY, pointer, button);
            return super.touchUp(screenX, screenY, pointer, button);
        }

    }

    /**
     * @return the state if the player should be rendered
     */
    @Override
    public boolean isVisible() {
        return cameraMode != EntityPlayerSP.CameraMode.FIRST_PERSON;
    }

}
