package me.gommeantilegit.minecraft.gamesettings;

import com.badlogic.gdx.Input;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.gamesettings.settingtypes.*;
import me.gommeantilegit.minecraft.world.chunk.ChunkBase;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Collection of all settings
 */
public class GameSettings {

    /**
     * Parent Minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * {@link GeneralSettings} instance
     */
    @NotNull
    public final GeneralSettings generalSettings;

    /**
     * {@link VideoSettings} instance
     */
    @NotNull
    public final VideoSettings videoSettings;

    /**
     * {@link KeyBindings} instance
     */
    @NotNull
    public final KeyBindings keyBindings;

    public GameSettings(@NotNull ClientMinecraft mc) {
        this.mc = mc;
        this.generalSettings = new GeneralSettings();
        this.videoSettings = new VideoSettings();
        this.keyBindings = new KeyBindings();
    }

    /**
     * General game settings class.
     * Collection of the generalized game options. (eg. sound settings)
     */
    public static class GeneralSettings {

        /**
         * Percent setting for music volume. 0% means that music is disabled.
         */
        @NotNull
        public final PercentSetting music = new PercentSetting("Music").setValue(100.0)
                .addStringValueReplacement(0, "OFF");

        /**
         * Boolean setting whether the mouse player pitch changes should be inverted.
         */
        @NotNull
        public final BooleanSetting invertMouse = new BooleanSetting("Invert Mouse").setValue(false);

        /**
         * Game difficulty setting
         */
        @NotNull
        public final StringSelectionSetting difficultySetting = new StringSelectionSetting("Difficulty", new String[]{
                "Peaceful", "Easy", "Normal", "Hard"
        }).setValue("Normal");

        /**
         * Percent setting for music sound. 0% means that music is disabled.
         */
        @NotNull
        public final PercentSetting sound = new PercentSetting("Sound").setValue(100.0)
                .addStringValueReplacement(0, "OFF");

        /**
         * Percent setting for mouse sensitivity
         */
        @NotNull
        public final PercentSetting mouseSensitivity = new PercentSetting("Sensitivity", new LimitedNumberSetting.Interval(0, 200)).setValue(100.0).
                addStringValueReplacement(0, "*yawn*").addStringValueReplacement(200, "HYPERSPEED!!!");

        /**
         * Array of all general settings
         */
        @NotNull
        public Setting<?>[] settingsList = new Setting<?>[]{
                music, sound, invertMouse, mouseSensitivity, difficultySetting
        };
    }

    /**
     * Video settings class.
     * Collection of all Video Settings. (Graphics related game settings)
     */
    public class VideoSettings {

        /* Values arising from setting states */
        private boolean renderClouds, enableFog;

        /**
         * Graphics settings
         */
        @NotNull
        public final StringSelectionSetting graphics = new StringSelectionSetting("Graphics", new String[]{"Fast", "Fancy"})
                .addValueChangedListener(value -> {
                    switch (value) {
                        case "Fast": {
                            if (mc.theWorld != null) {
                                mc.theWorld.getWorldRenderer().setEnableFog(false);
                                mc.theWorld.getWorldRenderer().setRenderClouds(false);
                            }
                            renderClouds = false;
                            enableFog = false;
                            break;
                        }
                        case "Fancy": {
                            if (mc.theWorld != null) {
                                mc.theWorld.getWorldRenderer().setEnableFog(true);
                                mc.theWorld.getWorldRenderer().setRenderClouds(true);
                            }
                            renderClouds = true;
                            enableFog = true;
                            break;
                        }
                    }
                }).setValue("Fancy");


        /**
         * Render distance setting
         */
        @NotNull
        public final StringSelectionSetting renderDistance = new StringSelectionSetting("Render Distance", new String[]{"Far", "Normal", "Short", "Tiny"})
                .addValueChangedListener(value -> {
                    if (mc.theWorld != null) mc.theWorld.setChunkLoadingDistance(determineChunkLoadingDistance(value));
                }).setValue("Normal");

        /**
         * Setting whether the game frame rate should be limited
         */
        @NotNull
        public final BooleanSetting limitFrameRate = new BooleanSetting("Limit Framerate").setValue(false);

        /**
         * Setting whether the 3D effect should be enabled
         */
        @NotNull
        public final BooleanSetting enable3D = new BooleanSetting("3D Anaglyph").setValue(false);

        /**
         * Setting whether view bobbing should be enabled
         */
        @NotNull
        public final BooleanSetting viewBobbing = new BooleanSetting("View Bobbing").setValue(true);


        /**
         * Setting whether Smoothed Lighting should be enabled
         */
        @NotNull
        public final BooleanSetting smoothLighting = new BooleanSetting("Smooth Lighting").setValue(true);

        /**
         * Array of all video settings
         */
        @NotNull
        public Setting<?>[] settingsList = {
                graphics, renderDistance, limitFrameRate, enable3D, viewBobbing, smoothLighting
        };

        /**
         * @return true if the game settings determine that fog should be enabled (Graphics = "Fancy")
         */
        public boolean getEnableFog() {
            return enableFog;
        }

        /**
         * @return true if the game settings determine that clouds should be rendered (Graphics = "Fancy")
         */
        public boolean getRenderClouds() {
            return renderClouds;
        }

        /**
         * @return the chunk loading distance in blocks according to the render / chunk loading distance setting
         */
        public int determineChunkLoadingDistance() {
            return determineChunkLoadingDistance(this.renderDistance.getValue());
        }

        /**
         * @param settingValue the value of the render distance setting
         * @return the minimum distance to a chunk in blocks
         */
        private int determineChunkLoadingDistance(@NotNull String settingValue) {
            switch (settingValue) {
                case "Tiny":
                    return 2 * ChunkBase.CHUNK_SIZE;
                case "Short":
                    return 8 * ChunkBase.CHUNK_SIZE;
                case "Normal":
                    return 16 * ChunkBase.CHUNK_SIZE;
                case "Far":
                    return 32 * ChunkBase.CHUNK_SIZE;
                default:
                    return 1;
            }
        }
    }

    /**
     * Key-bindings class
     * Collection of all key-bind settings
     */
    public static class KeyBindings {

        @NotNull
        public final KeyBindSetting keyBindForward = new KeyBindSetting("Forward", Input.Keys.W);

        @NotNull
        public final KeyBindSetting keyBindLeft = new KeyBindSetting("Left", Input.Keys.A);

        @NotNull
        public final KeyBindSetting keyBindBack = new KeyBindSetting("Back", Input.Keys.S);

        @NotNull
        public final KeyBindSetting keyBindRight = new KeyBindSetting("Right", Input.Keys.D);

        @NotNull
        public final KeyBindSetting keyBindJump = new KeyBindSetting("Jump", Input.Keys.SPACE);

        @NotNull
        public final KeyBindSetting keyBindSprint = new KeyBindSetting("Sprint", Input.Keys.CONTROL_LEFT);

        @NotNull
        public final KeyBindSetting keyBindInventory = new KeyBindSetting("Intentory", Input.Keys.E);

        @NotNull
        public final KeyBindSetting keyBindDrop = new KeyBindSetting("Drop", Input.Keys.Q);

        @NotNull
        public final KeyBindSetting keyBindChat = new KeyBindSetting("Chat", Input.Keys.T);

        @NotNull
        public final KeyBindSetting keyBindSneak = new KeyBindSetting("Sneak", Input.Keys.SHIFT_LEFT);

        /**
         * List of all keybinds
         */
        @NotNull
        public final KeyBindSetting[] keyBindList = {
                keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSprint, keyBindSneak, keyBindDrop, keyBindInventory, keyBindChat
        };
    }
}
