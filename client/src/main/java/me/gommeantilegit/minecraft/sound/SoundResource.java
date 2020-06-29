package me.gommeantilegit.minecraft.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import me.gommeantilegit.minecraft.utils.LazyProperty;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/**
 * Represents a playable sound with variations
 */
public class SoundResource {

    /**
     * The string that all variations of the sound resource start with
     */
    @NotNull
    private final String soundPath;

    /**
     * List of sound resources
     */
    @NotNull
    public static final List<LazyProperty<SoundResource>> SOUND_RESOURCES = new ArrayList<>();

    /**
     * Maps the sound paths to their parent sound resources wrapped in a lazy property to be evaluated on first access
     */
    @NotNull
    public static final Map<String, LazyProperty<SoundResource>> RESOURCE_MAP = new HashMap<>();

    /**
     * Initializes the sounds
     */
    public static void initSounds() {
        if (!SOUND_RESOURCES.isEmpty())
            throw new IllegalStateException("SoundResources already initialized!");
        String[] soundPaths = Gdx.files.classpath("sound/sounds.txt").readString(StandardCharsets.UTF_8.name()).replace("\r", "").split("\n");
        for (String soundPath : soundPaths) {
            LazyProperty<SoundResource> lazyProperty = new LazyProperty<>(() -> new SoundResource(soundPath));
            SOUND_RESOURCES.add(lazyProperty);
            RESOURCE_MAP.put(soundPath, lazyProperty);
        }
    }

    /**
     * @param soundPath the {@link SoundResource#soundPath} of a given sound resource instance
     * @return the {@link SoundResource} instance with a soundStartPath of the specified parameter
     */
    @NotNull
    public static SoundResource getSound(@NotNull String soundPath) {
        LazyProperty<SoundResource> property = RESOURCE_MAP.get(soundPath);
        if (property == null)
            throw new IllegalArgumentException("No such sound: " + soundPath);
        return property.get();
    }

    @NotNull
    public String getSoundPath() {
        return soundPath;
    }

    /**
     * Represents a variation of the sound.
     * A Variation is a sound that is parent to a given superordinate sound resource. When a sound resource is played, a sound variant is randomly chosen to be played
     */
    public static class SoundVariation {

        /**
         * The the playable libgdx sound instance
         */
        @NotNull
        private final Sound sound;

        /**
         * Sound duration in milliseconds
         */
        public final long duration;

        public SoundVariation(@NotNull Sound sound, long duration) {
            this.sound = sound;
            this.duration = duration;
        }

        public long getDuration() {
            return duration;
        }

        @NotNull
        public Sound getSound() {
            return sound;
        }
    }

    /**
     * The random instance to chose the sound variation to play
     */
    @NotNull
    private static final Random RANDOM = new Random();

    /**
     * Sound identification
     */
    public final short id;

    /**
     * The list of variations the sounds has
     */
    @NotNull
    private final List<SoundVariation> variations = new ArrayList<>();

    /**
     * @param soundPath the string that all the sounds variations start with
     */
    SoundResource(@NotNull String soundPath) {
        this.soundPath = soundPath;
        this.id = SoundResource.index();
        List<String> variationResourceStrings = getSounds().keySet().stream().filter(p -> p.startsWith(soundPath)).collect(Collectors.toList());
        for (String soundResourcePath : variationResourceStrings) {
            FileHandle fileHandle = Gdx.files.classpath(soundResourcePath);
            this.addVariation(new SoundVariation(Gdx.audio.newSound(fileHandle), getDuration(soundResourcePath)));
        }
        if (this.variations.size() == 0)
            throw new IllegalStateException("Sound \"" + soundPath + "\" resource must at least have one variation!");
    }


    /**
     * Adds the given sound variation to the list of sound variations of this sound resource object
     *
     * @param variation the variation to be added
     */
    private void addVariation(@NotNull SoundVariation variation) {
        this.variations.add(variation);
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
     * Stores the sound resource path and their parent duration in ms.
     */
    @NotNull
    private static HashMap<String, Long> sounds = getSoundLengths();

    @NotNull
    private static HashMap<String, Long> getSoundLengths() {
        String[] split = Gdx.files.classpath("sound/sound_lengths.txt").readString("UTF-8").split("\r\n");
        HashMap<String, Long> map = new HashMap<>();
        for (String s : split) {
            String[] args = s.split("=");
            map.put(args[0], Long.valueOf(args[1]));
        }
        return map;
    }

    private static long getDuration(@NotNull String soundResource) {
        return sounds.get(soundResource);
    }

    /**
     * Plays a random variation of the sound at volume 1f. without any 3D effects.
     */
    public void play() {
        getRandomVariation().getSound().play();
    }

    /**
     * Plays a random variation of the sound at the specified volume without any 3D effects
     *
     * @param volume the volume that the sound should be played at
     */
    public void play(float volume) {
        getRandomVariation().getSound().play(volume);
    }

    /**
     * Plays a random variation of the sound at the specified volume with the pitch without any 3D effects
     *
     * @param volume the volume that the sound should be played at
     * @param pitch  the pitch that the sound should be played with
     */
    public void play(float volume, float pitch) {
        getRandomVariation().getSound().play(volume, pitch, 0); // Playing sound in center with the specified volume and pitch
    }

    /**
     * Plays a random variation of the sound at the specified volume with the pitch without any 3D effects
     *
     * @param volume the volume that the sound should be played at
     * @param pitch  the pitch that the sound should be played with
     * @param pan    the pan that the sound should play with. Pan controls stereo sound direction on a value scale of [-1, 1] where -1 is left, 0 is centered and 1 is right
     */
    public void play(float volume, float pitch, float pan) {
        getRandomVariation().getSound().play(volume, pitch, pan); // Playing sound in center with the specified volume and pitch
    }


    /**
     * Plays a random variation of the sound at the specified volume with the specified pitch at the specified position relative to the specified listener position accordingly to the look direction of the listener
     *
     * @param volume                the volume that the sound should be played at
     * @param pitch                 the pitch that the sound should be played with
     * @param soundPosition         the position where the sound is played.
     * @param listenerPosition      the position where the listener is standing.
     * @param listenerLookDirection the 3D direction vector where the listener is facing.
     */
    public void playRelative(float volume, float pitch, @NotNull Vector3 soundPosition, @NotNull Vector3 listenerPosition, @NotNull Vector3 listenerLookDirection) {
        float pan = calculatePan(listenerLookDirection, listenerPosition, soundPosition);
        float adjustedVoume = calculateVolume(soundPosition, listenerPosition, volume);
        play(adjustedVoume, pitch, pan);
    }


    /**
     * @return a randomly chosen sound variation of the given sound resource
     */
    @NotNull
    public SoundVariation getRandomVariation() {
        return this.variations.get(RANDOM.nextInt(variations.size()));
    }

    @NotNull
    public List<SoundVariation> getVariations() {
        return variations;
    }

    @NotNull
    public static HashMap<String, Long> getSounds() {
        return sounds;
    }

    /**
     * @param soundPosition    the position where the sound is played
     * @param listenerPosition the position where the listener is standing
     * @param volume           the volume that the sound emits
     * @return the volume of the sound according to the distance from hearer and sound emitter position
     */
    private float calculateVolume(@NotNull Vector3 soundPosition, @NotNull Vector3 listenerPosition, float volume) {
        float dst = soundPosition.dst(listenerPosition);
        return (float) (volume - abs(20 * log(0.01f / dst)));
    }

    /**
     * @param listenerLookDirection the direction that the listener is facing in
     * @param soundPosition         the position where the sound is played
     * @param listenerPosition      the position where the listener is standing
     * @return the pan that indicates from what direction the sound is coming from.
     * @see com.badlogic.gdx.audio.Sound#play(float, float, float)
     */
    private float calculatePan(@NotNull Vector3 listenerLookDirection, @NotNull Vector3 listenerPosition, @NotNull Vector3 soundPosition) {
        Vector3 up = Vector3.Y;
        return listenerLookDirection.cpy().crs(up).dot(soundPosition.cpy().sub(listenerPosition).nor());
    }
}
