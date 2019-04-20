package me.gommeantilegit.minecraft.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Represents a playable resource
 */
public enum SoundResource {

    /* All playable minecraft sounds */

    GUI_BUTTON_CLICK("sound/random/click.ogg");

    /**
     * The the playable libgdx sound instance
     */
    @NotNull
    private final Sound sound;

    /**
     * Sound duration in milliseconds
     */
    public final long duration;

    /**
     * Sound identification
     */
    public final short id;

    SoundResource(@NotNull String classPathResource) {
        FileHandle fileHandle = Gdx.files.classpath(classPathResource);
        this.sound = Gdx.audio.newSound(fileHandle);
        this.id = SoundResource.index();
        this.duration = getDuration(classPathResource);
    }

    /**
     * Incrementing helper variable for SoundResource ID assignation
     */
    private static short i;

    /**
     * @return the value i and increments it
     */
    private static short index() {
        return i++;
    }

    /**
     * Stores the sound resource ('/' becomes '.') and their parent duration in ms.
     */
    private static HashMap<String, Long> sounds;

    @NotNull
    private static HashMap<String, Long> getSoundLengths() {
        String[] split = Gdx.files.classpath("sound/sounds.txt").readString("UTF-8").split("\n");
        HashMap<String, Long> map = new HashMap<>();
        for (String s : split) {
            String[] args = s.split("=");
            map.put(args[0], Long.valueOf(args[1]));
        }
        return map;
    }

    private static long getDuration(@NotNull String soundResource) {
        if(sounds == null)
            sounds = getSoundLengths();
        return sounds.get(soundResource);
    }

    /**
     * Called to initialize the enum values.
     */
    public static void init() {
    }

    /**
     * Plays the given sound at volume 1f. without any 3D effects.
     */
    public void play() {
        sound.play();
    }

    public void play(float volume) {
        sound.play(volume);
    }

    public void play(float volume, float pitch) {
        sound.play(volume, pitch, 0); // Playing sound in center with the specified volume and pitch
    }

    public void playRelative(@NotNull SoundResource soundEngine, float volume, float pitch, @NotNull Vector3 soundPosition, @NotNull Vector3 hearPosition, @NotNull Vector3 lookDirection) {
        soundEngine.playRelative(this, volume, pitch, soundPosition, hearPosition, lookDirection);
    }

    @NotNull
    public Sound getSound() {
        return sound;
    }
}
