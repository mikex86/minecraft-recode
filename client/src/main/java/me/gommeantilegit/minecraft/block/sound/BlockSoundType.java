package me.gommeantilegit.minecraft.block.sound;

import me.gommeantilegit.minecraft.Side;
import me.gommeantilegit.minecraft.annotations.SideOnly;
import me.gommeantilegit.minecraft.sound.SoundResource;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the sounds played on block breaking and stepping and placing
 */
@SideOnly(side = Side.CLIENT)
public class BlockSoundType {

    /**
     * The name of the sound to play (no file extension and no directories)
     */
    @NotNull
    public final String soundName;
    /**
     * The volume at which the sound should be played
     */
    public final float volume;

    /**
     * The pitch at which the sound should be played
     */
    public final float pitch;

    public BlockSoundType(@NotNull String soundName, float volume, float pitch) {
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    @NotNull
    public String getSoundName() {
        return soundName;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    /**
     * @return the string that all {@link me.gommeantilegit.minecraft.sound.SoundResource.SoundVariation}s of the {@link me.gommeantilegit.minecraft.sound.SoundResource} start with that should be played on block breaking
     */
    @NotNull
    protected String getBreakSoundResourceStartString() {
        return "sound/dig/" + this.soundName;
    }

    /**
     * @return the string that all {@link me.gommeantilegit.minecraft.sound.SoundResource.SoundVariation}s of the {@link me.gommeantilegit.minecraft.sound.SoundResource} start with that should be played on block stepping
     */
    @NotNull
    protected String getStepSoundResourceStartString() {
        return this.getBreakSoundResourceStartString();
    }

    /**
     * @return the string that all {@link me.gommeantilegit.minecraft.sound.SoundResource.SoundVariation}s of the {@link me.gommeantilegit.minecraft.sound.SoundResource} start with that should be played on block placing
     */
    @NotNull
    protected String getPlaceSoundResourceStartString() {
        return this.getBreakSoundResourceStartString();
    }

    /**
     * @return the sound resource instance for {@link #getBreakSoundResourceStartString()}
     */
    @NotNull
    public SoundResource getBreakSound() {
        return SoundResource.getSound(getBreakSoundResourceStartString());
    }

    /**
     * @return the sound resource instance for {@link #getStepSoundResourceStartString()}
     */
    @NotNull
    public SoundResource getStepSound() {
        return SoundResource.getSound(getStepSoundResourceStartString());
    }

    /**
     * @return the sound resource instance for {@link #getPlaceSoundResourceStartString()}
     */
    @NotNull
    public SoundResource getPlaceSound() {
        return SoundResource.getSound(getPlaceSoundResourceStartString());
    }
}