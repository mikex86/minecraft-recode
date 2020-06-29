import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.utils.Array;
import me.gommeantilegit.minecraft.AbstractMinecraft;
import me.gommeantilegit.minecraft.ClientMinecraft;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.gommeantilegit.minecraft.AbstractMinecraft.MINECRAFT_VERSION_PREFIX_CHAR;
import static me.gommeantilegit.minecraft.AbstractMinecraft.MINECRAFT_VERSION_STRING;

public class MinecraftRunner {

    /**
     * Stores all opened windows.
     * Should only contain one window.
     */
    @NotNull
    private static final ArrayList<Lwjgl3Window> WINDOWS = new ArrayList<>();

    public static void runMinecraft() throws ClassNotFoundException {
        Class.forName(AbstractMinecraft.class.getName());
        Thread openGLThread = new Thread("OpenGL-Thread") {

            @Override
            public void run() {
                Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                config.useVsync(false);
//                config.useOpenGL3(true, 3, 2);
                config.setWindowIcon(Files.FileType.Classpath, "icons/icon_32x32.png");
                config.setTitle("Minecraft " + MINECRAFT_VERSION_PREFIX_CHAR + MINECRAFT_VERSION_STRING);
                config.setIdleFPS(30);
                config.setWindowedMode(854, 480);
                config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 0);
                new MinecraftApp(config);
            }
        };
        openGLThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        openGLThread.setPriority(Thread.MAX_PRIORITY);
        openGLThread.start();
    }

    static class MinecraftApp extends Lwjgl3Application {
        MinecraftApp(Lwjgl3ApplicationConfiguration config) {
            super(new ApplicationAdapter() {

                private ClientMinecraft mc;

                @Override
                @SuppressWarnings("unchecked")
                public void create() {
                    Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
                    try {
                        Field windowsField = Lwjgl3Application.class.getDeclaredField("windows");
                        windowsField.setAccessible(true);
                        Array<Lwjgl3Window> windows = (Array<Lwjgl3Window>) windowsField.get(app);
//                        WINDOWS.addAll(Arrays.stream(windows.items).filter(Objects::nonNull).collect(Collectors.toList()));
                        for (int i = 0; i < windows.size; i++) {
                            WINDOWS.add(windows.get(i));
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    mc = new ClientMinecraft() {
                        @Override
                        protected void close() {
                            for (Lwjgl3Window window : WINDOWS) {
                                window.closeWindow();
                            }
                        }
                    };
                    mc.create();
                }

                @Override
                public void resize(int width, int height) {
                    mc.resize(width, height);
                }

                @Override
                public void render() {
                    mc.render();
                }

                @Override
                public void pause() {
                    mc.pause();
                }

                @Override
                public void resume() {
                    mc.resume();
                }

                @Override
                public void dispose() {
                    mc.dispose();
                }
            }, config);
        }
    }


}
