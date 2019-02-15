import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import me.gommeantilegit.minecraft.Minecraft;

public class DesktopLauncher {

    public static void main(String[] arg) {
        new Thread("OpenGL-Thread"){
            @Override
            public void run() {
                Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                config.useVsync(false);
                config.setTitle("Minecraft");
                config.setWindowedMode(854, 480);
                config.setBackBufferConfig(8, 8, 8, 8, 24, 0, 0);
                new Lwjgl3Application(new Minecraft(), config);
            }
        }.start();
    }
}
