package me.gommeantilegit.minecraft.sound;

import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.utils.Clock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/**
 * The sound engine handling surround sound
 */
public class SoundEngine {

    /**
     * Speed of sound (343 m/s) in blocks per second.
     */
    private static final float SPEED_OF_SOUND = 6.8599997f;

    /**
     * List of all currently startedPlaying sounds
     */
    @NotNull
    private final ArrayList<PlayingSound> playingSounds = new ArrayList<>();

    public void playRelative(@NotNull SoundResource soundResource, float volume, float pitch, @NotNull Vector3 soundPosition, @NotNull Vector3 hearPosition, @NotNull Vector3 lookDirection) {
        synchronized (playingSounds) {
            this.playingSounds.add(new PlayingSound(soundResource, volume, pitch, soundPosition, hearPosition, lookDirection));
        }
    }

    /**
     * Changes the pitches of a startedPlaying sound to the interpolation between the last and current tick's values
     *
     * @param partialTicks timer partial ticks
     */
    public void update(float partialTicks) {
        synchronized (playingSounds) {
            for (PlayingSound playingSound : this.playingSounds) {
                playingSound.update(partialTicks);
            }
        }
    }

    /**
     * Updates the startedPlaying sounds if their position has changed or if the hearer position has changed
     */
    public void tick(float partialTicks) {
        synchronized (playingSounds) {
            for (int i = 0; i < playingSounds.size(); i++) {
                PlayingSound playingSound = playingSounds.get(i);
                if (playingSound.updateValues()) {
                    playingSounds.remove(i--);
                }
            }
        }
    }

    /**
     * Represents a sound that is currently startedPlaying
     */
    public class PlayingSound {

        /**
         * The initial volume and pitch that are used as calculation basis for the doppler effect and volume calculations in 3D Space
         */
        private final float volume, pitch;

        /**
         * The 3D Vector that is constantly changed to the position where the sound is coming from.
         * The changes to this vector are not made by the engine but the caller of the {@link #playRelative(SoundResource, float, float, Vector3, Vector3, Vector3)} method
         */
        @NotNull
        private final Vector3 updatingSoundPosition;

        /**
         * The 3D Vector that is constantly changed to the position where the sound is heard from.
         * The changes to this vector are not made by the engine but the caller of the {@link #playRelative(SoundResource, float, float, Vector3, Vector3, Vector3)} method
         */
        @NotNull
        private final Vector3 updatingHearPosition;


        /**
         * The 3D Vector that is constantly changed to the direction that the listener faces.
         * The changes to this vector are not made by the engine but the caller of the {@link #playRelative(SoundResource, float, float, Vector3, Vector3, Vector3)} method
         */
        @NotNull
        private final Vector3 updatingLookDirection;

        /**
         * Position where the emitter was last tick
         */
        @NotNull
        private final Vector3 lastSoundPosition = new Vector3();

        /**
         * The sound resource that is being played
         */
        @NotNull
        private final SoundResource soundResource;


        /**
         * The libGDX sound handle that represents the startedPlaying sound
         */
        private long playingSoundHandle = -1;

        /**
         * Sound values of last tick
         */
        private float lastPan, lastPitch, lastVolume;

        /**
         * Sound values of current tick
         */
        private float currentPan, currentPitch, currentVolume;

        /**
         * Time util for timing if the sound is still startedPlaying
         */
        @NotNull
        private final Clock timer = new Clock(false);

        /**
         * State if the sound has started startedPlaying
         */
        private boolean startedPlaying = false;

        public PlayingSound(@NotNull SoundResource soundResource, float volume, float pitch, @NotNull Vector3 updatingSoundPosition, @NotNull Vector3 updatingHearPosition, Vector3 updatingLookDirection) {
            this.volume = volume;
            this.pitch = pitch;
            this.updatingSoundPosition = updatingSoundPosition;
            this.updatingHearPosition = updatingHearPosition;
            this.updatingLookDirection = updatingLookDirection;
            this.soundResource = soundResource;
            this.lastSoundPosition.set(updatingSoundPosition);
        }

        /**
         * Updates sound values like pan, pitch and volume on tick.
         *
         * @return true if the sound has stopped playing else false.
         */
        public boolean updateValues() {
            this.lastPan = this.currentPan;
            this.lastPitch = this.currentPitch;
            this.lastVolume = this.currentVolume;
            this.lastSoundPosition.set(updatingSoundPosition);
            this.currentPan = calculatePan();
            this.currentPitch = calculateDopplerEffect();
            this.currentVolume = calculateVolume();
            return startedPlaying && this.timer.getTimePassed() >= soundResource.duration;
        }

        /**
         * @param partialTicks the timer partial ticks
         */
        public void update(float partialTicks) {
            float volume = lastVolume + (currentVolume - lastVolume) * partialTicks, pitch = lastPitch + (currentPitch - lastPitch) * partialTicks, pan = lastPan + (currentPan - lastPan) * partialTicks;
            if (playingSoundHandle == -1) {
                this.soundResource.getSound().play(volume, pitch, pan);
                this.timer.reset();
                this.startedPlaying = true;
            } else {
                this.soundResource.getSound().setPitch(playingSoundHandle, pitch);
                this.soundResource.getSound().setVolume(playingSoundHandle, volume);
                this.soundResource.getSound().setPitch(playingSoundHandle, pitch);
            }
        }

        /**
         * @return the volume of the sound according to the distance from hearer and sound emitter position
         */
        private float calculateVolume() {
            float dst = this.updatingSoundPosition.dst(updatingHearPosition);
            return (float) (volume - abs(20 * log(0.01f / dst)));
        }

        /**
         * @return the pitch that the sound must have according to speed of hearer and sound emitter
         */
        private float calculateDopplerEffect() {
            float deltaPitch = ((lastSoundPosition.dst(updatingSoundPosition)) / SPEED_OF_SOUND) * pitch; // Speed is represented by distance as the time is asserted to be one tick
            return volume + deltaPitch;
        }

        /**
         * @return the pan that indicates from what direction the sound is coming from.
         * @see com.badlogic.gdx.audio.Sound#play(float, float, float)
         */
        private float calculatePan() {
            Vector3 up = Vector3.Y;
            return this.updatingLookDirection.crs(up).dot(this.updatingSoundPosition.cpy().sub(this.updatingHearPosition).nor());
        }
    }
}
