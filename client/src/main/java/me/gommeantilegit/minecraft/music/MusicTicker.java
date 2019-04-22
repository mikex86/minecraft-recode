package me.gommeantilegit.minecraft.music;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import me.gommeantilegit.minecraft.ClientMinecraft;
import me.gommeantilegit.minecraft.timer.api.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static java.lang.Math.*;

/**
 * The object that decides what music to play when
 */
public class MusicTicker implements Tickable {

    /**
     * Parent random instance
     */
    @NotNull
    private final Random random = new Random();

    /**
     * Parent minecraft instance
     */
    @NotNull
    private final ClientMinecraft mc;

    /**
     * Time until the next music plays in ticks
     */
    private int timeUntilNextMusic = 100;

    /**
     * The type of music that is currently playing
     */
    @Nullable
    private MusicType currentlyPlayingMusicType = null;

    /**
     * The music instance that is currently playing
     */
    @Nullable
    private Music playingMusic;

    /**
     * State whether music is currently fading out / in
     */
    private boolean fadingOut = false, fadingIn = false;

    public MusicTicker(@NotNull ClientMinecraft mc) {
        this.mc = mc;
        MusicType.GAME.toString(); // Initializing Enum Values
    }

    @Override
    public void tick(float partialTicks) {
        if (playingMusic != null && playingMusic.isPlaying() && !fadingOut && !fadingIn) {
            playingMusic.setVolume((float) mc.gameSettings.generalSettings.music.getRelativeValue());
        }
        if (playingMusic != null && playingMusic.isPlaying() && fadingIn) {
            if (playingMusic.getVolume() < mc.gameSettings.generalSettings.music.getRelativeValue()) {
                this.playingMusic.setVolume(min(1f, this.playingMusic.getVolume() + 0.01f));
            } else {
                this.playingMusic.setVolume(max(0f, this.playingMusic.getVolume() - 0.01f));
            }
            if (abs(playingMusic.getVolume() - mc.gameSettings.generalSettings.music.getRelativeValue()) < 0.01f) {
                this.fadingIn = false;
            }
        }

        MusicType currentAmbientMusicType = getAmbientMusicType();
        if (this.currentlyPlayingMusicType != null) {


            // Music stopped
            if (playingMusic != null && !playingMusic.isPlaying() && currentAmbientMusicType != null) {
                this.playingMusic = null;
                this.currentlyPlayingMusicType = null;
                this.timeUntilNextMusic = Math.min(currentAmbientMusicType.getMinDelay() + this.random.nextInt(currentAmbientMusicType.getMaxDelay()) + 1, this.timeUntilNextMusic);
            }

            // Ambient music type changed
            if (currentlyPlayingMusicType != null && currentlyPlayingMusicType != currentAmbientMusicType && currentAmbientMusicType != null && playingMusic != null && playingMusic.isPlaying()) {
                fadeOutPlayingMusic();
                this.timeUntilNextMusic = this.random.nextInt(currentAmbientMusicType.getMinDelay() / 2 + 1);
            }

            if (playingMusic == null && currentAmbientMusicType != null) {
                this.currentlyPlayingMusicType = null;
            }
        }

        if (this.currentlyPlayingMusicType == null && this.timeUntilNextMusic-- <= 0 && currentAmbientMusicType != null) {
            if (this.playingMusic != null && this.playingMusic.isPlaying()) {
                mc.runOnGLContextWait(new FutureTask<>((Callable<Void>) () -> {
                    this.playingMusic.stop();
                    return null;
                }));
                System.out.println("Stopped previously playing music to start new one. In general this should never happen!");
            }
            mc.runOnGLContextWait(new FutureTask<>((Callable<Void>) () -> {
                try {
                    this.startPlayMusic(currentAmbientMusicType);
                } catch (GdxRuntimeException e) {
                    System.out.println("Failed to start music! Ignoring because LibGDX is causing this problem and in general the music should start playing on next try.");
                }
                return null;
            }));
        }
    }

    /**
     * Fades out the currently playing music until it is silenced to be finally stopped and nullified.
     */
    private void fadeOutPlayingMusic() {
        this.fadingIn = false;
        this.fadingOut = true;
        if (this.playingMusic != null) {
            this.playingMusic.setVolume(max(0f, this.playingMusic.getVolume() - 0.01f));
            if (playingMusic.getVolume() == 0) {
                mc.runOnGLContext(new FutureTask<>((Callable<Void>) () -> {
                    playingMusic.stop();
                    return null;
                }));
                playingMusic = null;
                currentlyPlayingMusicType = null;
                this.fadingOut = false;
            }

        }
    }

    /**
     * Starts to play random music of the specified music type
     *
     * @param type the type of music to play
     */
    private void startPlayMusic(@NotNull MusicType type) {
        System.out.println("Start playing music: " + type.name());
        this.currentlyPlayingMusicType = type;
        Music randomMusic = type.getRandomMusic(random);
        randomMusic.play();
        randomMusic.setLooping(false);
        randomMusic.setVolume(0.1f);
        this.playingMusic = randomMusic;
        this.fadingIn = true;
        this.timeUntilNextMusic = Integer.MAX_VALUE;
    }

    /**
     * @return the current type of music to play
     */
    @Nullable
    private MusicType getAmbientMusicType() {
        //TODO: IMPLEMENT MORE LOGIC WHEN DIMENSIONS ARE IMPLEMENTED
        if (mc.theWorld == null)
            return MusicType.MENU;
        else if (mc.thePlayer != null && mc.thePlayer.spawned)
            return MusicType.GAME;
        else
            return null;
    }

    /**
     * The given types of music that can play
     */
    public enum MusicType {

        MENU("music/menu", 20, 600),
        GAME("music/game", 12000, 24000),
        CREATIVE("music/creative", 1200, 3600),
        CREDITS("music/credits", Integer.MAX_VALUE, Integer.MAX_VALUE),
        NETHER("music/nether", 1200, 3600),
        END_BOSS("music/end_boss", 0, 0),
        END("music/end", 6000, 24000);

        /**
         * Minimum and maximum delay between music in ticks
         */
        private final int minDelay, maxDelay;

        /**
         * All music instances that can be played in the given type
         */
        @NotNull
        private final Music[] playableMusic;

        MusicType(@NotNull String folderResourceString, int minDelay, int maxDelay) {
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
            ArrayList<FileHandle> subFiles = new ArrayList<>();
            // The music file names for the given music type are stored in a music.txt file in the according music type directory
            FileHandle musicTxt = Gdx.files.classpath(folderResourceString + "/music.txt");
            String[] split = musicTxt.readString(StandardCharsets.UTF_8.name()).split("\r\n");
            for (String musicFileStr : split) {
                subFiles.add(Gdx.files.classpath(folderResourceString + "/" + musicFileStr + ".ogg"));
            }
            this.playableMusic = new Music[(int) subFiles.stream().filter(m -> !m.isDirectory()).count()];
            int i = 0;
            for (FileHandle subFile : subFiles) {
                if (!subFile.isDirectory()) {
                    playableMusic[i++] = Gdx.audio.newMusic(subFile);
                }
            }
        }

        @NotNull
        public Music[] getPlayableMusic() {
            return playableMusic;
        }

        public int getMinDelay() {
            return minDelay;
        }

        public int getMaxDelay() {
            return maxDelay;
        }

        /**
         * @param random a random instance
         * @return randomly selected music from the this type
         */
        @NotNull
        public Music getRandomMusic(@NotNull Random random) {
            return this.playableMusic[random.nextInt(playableMusic.length)];
        }
    }
}
