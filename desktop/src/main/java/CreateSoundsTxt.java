import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CreateSoundsTxt {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setIdleFPS(30);
        config.setWindowedMode(854, 480);
        config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 0);
        new Lwjgl3Application(new ApplicationAdapter() {
            @Override
            public void create() {
                StringBuilder sb = new StringBuilder();
                ClassLoader cl = CreateSoundsTxt.class.getClassLoader();
                try {
                    File f = new File(Objects.requireNonNull(cl.getResource("sound")).toURI());
                    ArrayList<File> files = new ArrayList<>();
                    files.addAll(Arrays.asList(Objects.requireNonNull(f.listFiles())));
                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        if (file.isDirectory()) {
                            files.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles())));
                            files.remove(i--);
                            continue;
                        }
                    }
                    for (File file : files) {
                        if (file.getName().endsWith(".ogg")) {
                            OpenALSound sound = (OpenALSound) Gdx.audio.newSound(new FileHandle(file));
                            float duration = sound.duration();
                            sb.append("sound/").append(f.toPath().relativize(file.toPath()).toString().replace('\\', '/')).append("=").append((long) (duration * 1000)).append('\n');
                        }
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                System.out.println(sb);
            }
        }, config);
    }

}
