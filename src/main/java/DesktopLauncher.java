import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import me.michael.kei.minecraft.Minecraft;

public class DesktopLauncher {

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setWindowedMode(860, 480);
        new Lwjgl3Application(new Minecraft(), config);
    }
}
