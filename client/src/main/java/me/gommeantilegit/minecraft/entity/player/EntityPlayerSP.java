package me.gommeantilegit.minecraft.entity.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.block.Block;
import me.gommeantilegit.minecraft.block.Blocks;
import me.gommeantilegit.minecraft.block.sound.BlockSoundType;
import me.gommeantilegit.minecraft.block.state.IBlockState;
import me.gommeantilegit.minecraft.entity.player.controller.PlayerController;
import me.gommeantilegit.minecraft.entity.player.packet.PacketSender;
import me.gommeantilegit.minecraft.entity.player.skin.ClientSkin;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.KeyBindSetting;
import me.gommeantilegit.minecraft.hud.scaling.DPI;
import me.gommeantilegit.minecraft.phys.AxisAlignedBB;
import me.gommeantilegit.minecraft.raytrace.IRayTracer;
import me.gommeantilegit.minecraft.raytrace.RayTracer;
import me.gommeantilegit.minecraft.raytrace.RenderingRayTracer;
import me.gommeantilegit.minecraft.rendering.GLContext;
import me.gommeantilegit.minecraft.shader.programs.StdShader;
import me.gommeantilegit.minecraft.ui.button.Button;
import me.gommeantilegit.minecraft.ui.button.TexturedButton;
import me.gommeantilegit.minecraft.ui.render.Overlay2D;
import me.gommeantilegit.minecraft.ui.screen.impl.GuiIngamePause;
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
import java.util.Objects;

import static java.lang.Math.*;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

@SideOnly(side = Side.CLIENT)
public class EntityPlayerSP extends RenderablePlayer {

    /**
     * Controller object for player logic such as block breaking
     */
    @NotNull
    public final PlayerController playerController;

    @NotNull
    public final ClientMinecraft mc;

    /**
     * Ray-tracing utility.
     */
    public final RayTracer rayTracer;

    /**
     * Player perspective camera operating with position zero.
     * Does not update the frustum as it would not match the coordinate space
     */
    @NotNull
    public final PerspectiveCamera camera = new PerspectiveCamera(100, 0, 0) {

        @Nullable
        private Vector3 positionCopy = null;

        @Override
        public void update() {
            // lazy init because of update call from super constructor
            if (positionCopy == null) {
                positionCopy = new Vector3();
            }
            float aspect = viewportWidth / viewportHeight;

            far = 512;
            projection.setToProjection(abs(near), abs(far), fieldOfView, aspect);

            view.setToLookAt(position, positionCopy.set(position).add(direction), Vector3.Y);
            combined.set(projection);
            Matrix4.mul(combined.val, view.val);
        }

    };
    /**
     * Input processor for controlling the player
     */
    @NotNull
    public final EntityPlayerSP.PlayerInputAdapter camController = getCamController();
    /**
     * Object that sends packets to the server about the player's state
     */
    @NotNull
    private final PacketSender packetSender = new PacketSender(this);
    /**
     * Virtual Camera position
     */
    @NotNull
    private final Vector3 cameraPosition = new Vector3();
    /**
     * Virtual camera operating with the virtual camera position.
     * Used when the correct camera frustum is needed.
     */
    @NotNull
    public final PerspectiveCamera virtualCamera = new PerspectiveCamera(100, 0, 0) {

        @Nullable
        private Vector3 positionCopy = null;

        @Override
        public void update() {
            // lazy init because of update call from super constructor
            if (positionCopy == null) {
                positionCopy = new Vector3();
            }
            this.viewportWidth = camera.viewportWidth;
            this.viewportHeight = camera.viewportHeight;

            this.near = camera.near;
            this.far = camera.far;

            this.direction.set(camera.direction);
            this.position.set(cameraPosition);

            float aspect = viewportWidth / viewportHeight;
            projection.setToProjection(abs(near), abs(far), fieldOfView, aspect);
            view.setToLookAt(position, positionCopy.set(position).add(direction), up);
            combined.set(projection);
            Matrix4.mul(combined.val, view.val);

            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    };
    /**
     * The tick to frame interpolated position vector of the entity
     */
    @NotNull
    private final Vector3 interpolatedEntityPosition = new Vector3();
    /**
     * The position offset the view-bobbing occurs in relation to
     */
    @NotNull
    private final Vector3 viewBobPosOffset = new Vector3();
    /**
     * Camera direction temp vector for {@link #setupViewBobbing(float)}
     */
    @NotNull
    private final Vector3 cameraDirectionTmp = new Vector3();
    /**
     * Clock instance used to determine, if the player has stopped walking and quickly started again so sprinting can be toggled.
     */
    private final Clock sprintingStartClock = new Clock(false);
    /**
     * State whether the player has spawned in the world
     */
    public boolean spawned = false;
    /**
     * A 3D vector instance which values are updated to the player's {@link #posX}, {@link #posY} and {@link #posZ} every tick
     */
    @NotNull
    private Vector3 updatedPositionVector = new Vector3();
    /**
     * Camera mode
     */
    @NotNull
    private EntityPlayerSP.CameraMode cameraMode = CameraMode.FIRST_PERSON;
    /**
     * State whether the player has already received a PositionSetPacket
     */
    private boolean posPacketReceived = false;
    private boolean breakingBlocks = false;

    private boolean flyingHack = false;

    public EntityPlayerSP(@Nullable ClientWorld world, @NotNull ClientMinecraft mc, @NotNull String username, @NotNull ClientSkin skin) {
        super(world, 20, username, skin);
        this.mc = mc;

        rayTracer = new RenderingRayTracer(this);

        camera.viewportWidth = mc.width;
        camera.viewportHeight = mc.height;

        camera.near = 0.05f;
        camera.far = 512f;
        camera.update();

        playerController = new PlayerController(this, mc);
        if (GLContext.getGlContext().isCallingFromOpenGLThread())
            mc.shaderManager.stdShader.setCamera(camera);
        else
            GLContext.getGlContext().runOnGLContext(() -> mc.shaderManager.stdShader.setCamera(camera));
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
        // No synchronization on data received state: Try and see
        if (!posPacketReceived || currentChunk instanceof ClientChunk && !((ClientChunk) currentChunk).hasReceivedData()) {
            mc.thePlayer.motionY *= 0;
        }
        if (!spawned) {
            if (posPacketReceived) {
//                if (currentChunk instanceof ClientChunk && ((ClientChunk) currentChunk).dataReceived) {
                spawned = true;
//                }
            }
        }
        if (posPacketReceived) {
            super.tick();
            this.playerController.onPlayerBlockDamage();
            this.packetSender.sendMovePackets();
        }

        if (flyingHack) {
            motionY *= 0;
            if (keyDown(mc.gameSettings.keyBindings.keyBindForward.getValue())) {
                motionX += (float) cos(toRadians(rotationYaw + 90)) * 1;
                motionZ += (float) -sin(toRadians(rotationYaw + 90)) * 1;
//            motionX = MathUtils.clamp(motionX, -3.9f * 1, 3.9f * 1);
//            motionX = MathUtils.clamp(motionX, -3.9f * 1, 3.9f * 1);
            }

            if (keyDown(mc.gameSettings.keyBindings.keyBindJump.getValue())) {
                motionY = 1;
            }
            if (keyDown(mc.gameSettings.keyBindings.keyBindSneak.getValue())) {
                motionY = -1;
            }
        }

        updatedPositionVector.set(posX, posY, posZ);
    }

    @Override
    public void onSpawned(@NotNull ChunkBase chunk) {
        super.onSpawned(chunk);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);
        updatedPositionVector.set(x, y, z);
        posPacketReceived = true;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        super.setRotation(yaw, pitch);
        this.camController.calculateLookDirection();
    }

    /**
     * Updates {@link #moveForward} and {@link #moveStrafing} accordingly to the pressed keys
     */
    private void updateMovementValues() {
        boolean forwards = keyDown(mc.gameSettings.keyBindings.keyBindForward.getValue());
        boolean backwards = keyDown(mc.gameSettings.keyBindings.keyBindBack.getValue());
        boolean left = keyDown(mc.gameSettings.keyBindings.keyBindLeft.getValue());
        boolean right = keyDown(mc.gameSettings.keyBindings.keyBindRight.getValue());
        boolean jump = keyDown(mc.gameSettings.keyBindings.keyBindJump.getValue());
        boolean sprint = keyDown(mc.gameSettings.keyBindings.keyBindSprint.getValue());
        boolean sneak = keyDown(mc.gameSettings.keyBindings.keyBindSneak.getValue());

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
            this.camController.setKeyDown(mc.gameSettings.keyBindings.keyBindSneak.getValue(), false);
        }

        if (this.isSprinting() && (this.moveForward < f || this.isCollidedHorizontally())) {
            this.setSprinting(false);
        }

        this.setSneaking(sneak);

        setJumping(jump);
    }

    @NotNull
    private EntityPlayerSP.PlayerInputAdapter getCamController() {
        switch (Gdx.app.getType()) {
            case Android:
            case iOS:
                return new MobilePlayerController(this, mc);
            case Desktop:
                return new DesktopPlayerController(this);
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

        this.interpolatedEntityPosition.set(
                entityRenderPosition.lastPosX + (entityRenderPosition.posX - entityRenderPosition.lastPosX) * partialTicks,
                entityRenderPosition.lastPosY + (entityRenderPosition.posY - entityRenderPosition.lastPosY) * partialTicks + getEyeHeight(),
                entityRenderPosition.lastPosZ + (entityRenderPosition.posZ - entityRenderPosition.lastPosZ) * partialTicks);

        switch (this.cameraMode) {

            case THIRD_PERSON: {
                interpolatedEntityPosition.add(camera.direction.x * -3, camera.direction.y * -3, camera.direction.z * -3);
                break;
            }

        }
        cameraPosition.set(interpolatedEntityPosition.x, interpolatedEntityPosition.y, interpolatedEntityPosition.z);
        mc.shaderManager.stdShader.setVirtualCameraPos(interpolatedEntityPosition.x, interpolatedEntityPosition.y, interpolatedEntityPosition.z);
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

    @Override
    protected void playStepSound(@NotNull BlockPos standingOn) {
        ChunkBase forPos = world.getNearChunkFor(this.currentChunk, standingOn.getX(), standingOn.getZ());
        IBlockState blockState = Objects.requireNonNull(forPos).getBlockState(standingOn);
        if (blockState == null)
            return;
        Block block = blockState.getBlock();

        BlockSoundType soundType;

        //TODO: WHEN SNOW IS IMPLEMENTED
//        BlockState above = world.getBlockState(standingOn.offset(EnumFacing.UP));
//        if (above != mc.blocks.snow) {
//            soundType = mc.blockSounds.getSoundType(mc.blocks.snow);
//        } else
//        if (!block.isLiquid()) { //TODO: WHEN FLUIDS ARE IMPLEMENTED
        soundType = mc.blockSounds.getSoundType(block);
        soundType.getStepSound().play(soundType.getVolume() * 0.15F, soundType.getPitch());
//        }
    }

    public void setupViewBobbing(float partialTicks) {
//        if (!onGround) return;
        float f = getDistanceWalkedModified() - getPrevDistanceWalkedModified();
        float f1 = -(getDistanceWalkedModified() + f * partialTicks);
        float f2 = prevCameraYaw + (cameraYaw - prevCameraYaw) * partialTicks;
        float f3 = prevCameraPitch + (cameraPitch - prevCameraPitch) * partialTicks;
        StdShader stdShader = this.mc.shaderManager.stdShader;

        viewBobPosOffset.set(
                        (float) sin(f1 * (float) PI) * f2 * 0.5F,
//                0,
                        (float) -abs(cos(f1 * (float) PI) * f2),
                        0
                )
                .rotate(-(rotationPitch), 1, 0, 0)
                .rotate(rotationYaw, 0, 1, 0);

        stdShader.translate(
                viewBobPosOffset.x, viewBobPosOffset.y, viewBobPosOffset.z
        );

        Vector3 direction = camera.direction;
        stdShader.rotate(direction.x * -1, 0, direction.z * -1, (float) (sin(f1 * (float) PI) * f2 * 3.0F));

        direction = this.cameraDirectionTmp.set(camera.direction).rotate(90, 0, -1, 0);

        stdShader.rotate(direction.x, 0, direction.z, (float) (abs(cos(f1 * (float) PI - 0.2F) * f2) * 5.0F));

        direction = this.cameraDirectionTmp.set(camera.direction).rotate(90, 0, -1, 0);
        stdShader.rotate(direction.x, 0, direction.z, f3);


        // old
//        stdShader.rotate(0, 0, 1, (float) (Math.sin(f1 * (float) Math.PI) * f2 * 3.0F));
//        stdShader.rotate(1, 0, 0, (float) (Math.abs(Math.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
//        stdShader.rotate(1, 0, 0, f3);
    }

    @NotNull // TODO: REMOVE
    public Vector3 getUpdatedPositionVector() {
        return this.updatedPositionVector;
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
     * @see RayTracer.RayTraceResult#hitSide
     */
    public void onBlockBroken(@NotNull Block block, @NotNull BlockPos pos, @NotNull EnumFacing facingDirection) {
        this.breakMouseOver();
    }

    /**
     * Invoked when a key is pressed
     *
     * @param keyCode the key-code that was just pressed down
     */
    private void onKeyDown(int keyCode) {
        if (keyCode == mc.gameSettings.keyBindings.keyBindForward.getValue()) {
            if (sprintingStartClock.getTimePassed() <= 300) {
                setSprinting(true);
            }
            sprintingStartClock.reset();
        } else if (keyCode == Input.Keys.ESCAPE) {
            mc.uiManager.displayGuiScreen(new GuiIngamePause());
        } else if (keyCode == mc.gameSettings.keyBindings.keyBindSwitchPerspective.getValue()) {
            this.cameraMode = cameraMode.next();
        } else if (keyCode == Input.Keys.F8) {
            this.flyingHack = !flyingHack;
        }
    }

    /**
     * Invoked when a key is released.
     *
     * @param keyCode the key-code that was released
     */
    private void onKeyUp(int keyCode) {

    }

    /**
     * Updates the breaking of the selected block
     *
     * @param pointer pointer touching down. -1, if desktop click
     */
    private void updateBlockBreaking(int pointer) {
        if ((pointer == -1 || !(((MobilePlayerController) this.camController)).pressingButtonWithRotationPointer)) {
            swingItem();
            RayTracer.RayTraceResult result = rayTracer.getRayTraceResult();
            if (result.type == RayTracer.RayTraceResult.EnumResultType.BLOCK)
                this.breakingBlocks = true;
        }
    }

    private void onBlockBreakingAbort() {
        this.breakingBlocks = false;
        this.playerController.onBlockBreakingAbort();
    }

    private void breakMouseOver() {
        RayTracer.RayTraceResult result = rayTracer.getRayTraceResult();
        if (result.type == RayTracer.RayTraceResult.EnumResultType.BLOCK) {
            assert result.getBlockPos() != null;
            int x = result.getBlockPos().getX(), y = result.getBlockPos().getY(), z = result.getBlockPos().getZ();
            ChunkBase chunk = world.getNearChunkFor(currentChunk, x, z);
            {
                BlockPos blockPos = new BlockPos(x, y, z);
                Objects.requireNonNull(chunk, "Cannot break mouse over block pos: " + blockPos + ". Chunk for position is null!");
            }
            IBlockState prevBlockState = chunk.getBlockState(x, y, z);
            assert prevBlockState != null;
            chunk.setBlock(x, y, z, null);
            mc.theWorld.getParticleEngine().spawnBlockBreakingParticles(x, y, z, prevBlockState.getBlock());
        }
    }

    /**
     * @return the state if the player should be rendered
     */
    @Override
    public boolean isVisible() {
        return cameraMode != CameraMode.FIRST_PERSON;
    }

    public boolean isBreakingBlocks() {
        return breakingBlocks;
    }

    @NotNull
    @Override
    public ClientSkin getSkin() {
        return (ClientSkin) super.getSkin();
    }

    /**
     * Enum representing all camera modes
     */
    public enum CameraMode {
        FIRST_PERSON, THIRD_PERSON;

        public CameraMode next() {
            return values()[(Arrays.asList(values()).indexOf(this) + 1) % values().length];
        }
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

        protected PlayerInputAdapter(@NotNull EntityPlayerSP player) {
            this.player = player;
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
                this.player.onKeyDown(keyCode);
            else this.player.onKeyUp(keyCode);
            this.keys.put(keyCode, state);
        }

        /**
         * Sets the cameras view direction to the players yaw and pitch
         */
        void calculateLookDirection() {
            PerspectiveCamera camera = this.player.camera;
            double yaw = toRadians(this.player.rotationYaw + 90);
            double pitch = toRadians(this.player.rotationPitch);

            double xzLen = cos(pitch);
            double x = (xzLen * cos(yaw));
            double y = -sin(pitch);
            double z = (xzLen * sin(-yaw));

            camera.lookAt((float) x, (float) y, (float) z);
        }

        /**
         * @param key a given keycode
         * @return if the key is currently being held down
         */
        boolean key(int key) {
            return this.keys.get(key) != null && this.keys.get(key);
        }

        /**
         * Renders an overlay of the controller, if needed
         */
        void render() {
        }
    }

    /**
     * Class Wrapper converting mouse and keyboard input into player rotation and movement
     */
    private static class DesktopPlayerController extends PlayerInputAdapter {

        private int lastX = Gdx.input.getX(), lastY = Gdx.input.getY();

        /**
         * @param player sets {@link #player}
         */
        DesktopPlayerController(@NotNull EntityPlayerSP player) {
            super(player);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            updateMouseInput(-screenX + lastX, screenY - lastY);
            lastX = screenX;
            lastY = screenY;
            return super.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            updateMouseInput(lastX - screenX, screenY - lastY);
            lastX = screenX;
            lastY = screenY;
            return super.mouseMoved(screenX, screenY);
        }

        /**
         * Updates mouse input -> converting to player rotation
         */
        private void updateMouseInput(int deltaX, int deltaY) {

            float relativeSensitivity = (float) player.mc.gameSettings.generalSettings.mouseSensitivity.getRelativeValue();
            float sensitivity = relativeSensitivity * 0.6F + 0.2F;
            float scl = sensitivity * sensitivity * sensitivity * 8.0f * 0.1f;

            float scaledDX = deltaX * scl;
            float scaledDY = deltaY * scl * (player.mc.gameSettings.generalSettings.invertMouse.getValue() ? -1 : 1);
//            Gdx.input.setCursorCatched(player.mc.uiManager.currentScreen == null);

            //X Rotation
            {
//                camera.direction.rotate(camera.up, deltaX);
//                tmp.set(camera.direction).crs(camera.up).nor();
                player.rotationYaw += scaledDX;
            }
            //Y Rotation
            {

                float newPitchDegrees = player.rotationPitch + scaledDY;
                newPitchDegrees = max(-89.9f, min(newPitchDegrees, 89.9f));
                player.rotationPitch = newPitchDegrees;
//                camera.direction.rotate(tmp, lastPitchDegrees - newPitchDegrees);
//                lastPitchDegrees = newPitchDegrees;
            }
            calculateLookDirection();
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
            if (button == 0) { // left click
                player.updateBlockBreaking(-1);
            } else if (button == 1) { // right click
                IRayTracer.RayTraceResult hitResult = player.rayTracer.getRayTraceResult();
                if (hitResult.type == IRayTracer.RayTraceResult.EnumResultType.BLOCK) {
                    BlockPos blockPos = Objects.requireNonNull(hitResult.getBlockPos(), "HitResult BlockPos returned null for block type result");
                    EnumFacing hitSide = Objects.requireNonNull(hitResult.getHitSide(), "HitResult HitSide returned null for block type result");
                    BlockPos newPos = blockPos.offset(hitSide);
                    AxisAlignedBB blockBoundingBox = new AxisAlignedBB(
                            newPos.getX(), newPos.getY(), newPos.getZ(),
                            newPos.getX() + 1, newPos.getY() + 1, newPos.getZ() + 1
                    );
                    if (player.world.getBlock(newPos) == null && !player.getBoundingBox().expand(abs(player.motionX), abs(player.motionY), abs(player.motionZ)).intersects(blockBoundingBox)) {
                        player.world.setBlock(newPos.getX(), newPos.getY(), newPos.getZ(), player.mc.getBlocks().stone.getDefaultBlockState()); // TODO: CHANGE WHEN INVENTORY IS IMPLEMETED
                    }
                }
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
     * Class Wrapper converting mobile touch input into player movement and rotation
     */
    private static class MobilePlayerController extends PlayerInputAdapter {

        /**
         * Temp vector for rotation calculation
         */
        @NotNull
        private final Vector3 tmp = new Vector3();
        /**
         * Minecraft instance
         */
        private final ClientMinecraft mc;
        /**
         * Timer for resetting the amount the pointer has moved every second
         */
        private final Clock blockBreakingClock = new Clock(false);
        /**
         * Button layout overlay instance
         */
        private final ButtonLayout buttonLayout = new ButtonLayout();
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
         * Delta time the player has been staring at a point while holding down the finger.
         */
        private float blockBreakDelta = 0;
        /**
         * State if the player has already broken a block in the current press of the pointer
         */
        private boolean breakingBlocks = false;

        /**
         * @param player sets {@link #player}
         * @param mc     sets {@link #mc}
         */
        MobilePlayerController(@NotNull EntityPlayerSP player, ClientMinecraft mc) {
            super(player);
            this.mc = mc;
        }

        /**
         * Input processing function invoked by {@link ButtonLayout}
         *
         * @param screenX cursor x coord
         * @param screenY cursor y coord
         * @param pointer pointer index
         */
        void onTouchDragged(int screenX, int screenY, int pointer) {
            float sensitivity = (float) player.mc.gameSettings.generalSettings.mouseSensitivity.getRelativeValue();

            float deltaX = (lastScreenX - screenX) * sensitivity;
            float deltaY = (lastScreenY - screenY) * sensitivity;

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
            /**
             * Pointer id which is actually controlling the player's rotation
             */
            private int rotationPointer = -1;
            /**
             * The amount of pixel the mouse has moved during the last second.
             */
            private int pointerMovedInLastSecond = 0;

            ButtonLayout() {
                super(mc.spriteBatch);
                this.buttons = new Button[]{forwardButton, leftButton, backwardsButton, rightButton, jumpButton};
                this.moveButtons = new Button[]{forwardButton, leftButton, backwardsButton, rightButton};
                {
                    applyKeyBind(forwardButton, mc.gameSettings.keyBindings.keyBindForward);
                    applyKeyBind(leftButton, mc.gameSettings.keyBindings.keyBindLeft);
                    applyKeyBind(backwardsButton, mc.gameSettings.keyBindings.keyBindBack);
                    applyKeyBind(rightButton, mc.gameSettings.keyBindings.keyBindRight);
                    applyKeyBind(jumpButton, mc.gameSettings.keyBindings.keyBindJump);
                    //Extra configuration for the jump button
                    {
                        jumpButton.setOnDragEnterListener(pointer -> {
                                    setKeyDown(mc.gameSettings.keyBindings.keyBindJump.getValue(), true);
                                    jumpButton.transparency = 0.70;
                                    pressingButtonWithRotationPointer = true;
                                }
                        );
                        jumpButton.setOnDragLeaveListener(pointer -> {
                                    setKeyDown(mc.gameSettings.keyBindings.keyBindJump.getValue(), false);
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
            private void applyKeyBind(TexturedButton button, KeyBindSetting keyBind) {
                button.transparency = 0.5;
                button.setOnDragEnterListener(pointer -> {
                            if (pointer != this.rotationPointer) {
                                setKeyDown(keyBind.getValue(), true);
                                button.transparency = 0.70;
                            }
                        }
                );
                button.setOnDragLeaveListener(pointer -> {
                            setKeyDown(keyBind.getValue(), false);
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

    }
}
