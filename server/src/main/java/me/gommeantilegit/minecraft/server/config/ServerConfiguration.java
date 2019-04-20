package me.gommeantilegit.minecraft.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.gommeantilegit.minecraft.ServerMinecraft;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Represents the server configuration
 */
public class ServerConfiguration {

    /**
     * The default maximum render distance a player is allowed to have
     */
    private static final int DEFAULT_MAX_RENDER_DISTANCE = 512;

    /**
     * The file that stores the configuration content
     */
    @NotNull
    private final File configurationFile = new File("config.json");

    /**
     * The maximum render distance that a player is allowed to have
     */
    private final int maxChunkLoadingDistance;

    public ServerConfiguration(@NotNull ServerMinecraft mc) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!configurationFile.exists()) {
            try {
                configurationFile.createNewFile();
            } catch (IOException e) {
                mc.logger.crash("Failed to create configuration file!", e);
            }
            JsonObject object = getDefaultConfigurationSettings();
            FileWriter writer = new FileWriter(configurationFile);
            writer.write(gson.toJson(object));
            writer.close();
        }
        try {
            JsonObject object = gson.fromJson(new BufferedReader(new FileReader(configurationFile)), JsonObject.class);
            this.maxChunkLoadingDistance = object.get("maxChunkLoadingDistance").getAsInt();
        } catch (JsonSyntaxException | ClassCastException | NullPointerException e) {
            mc.logger.crash("Your json file is formatted incorrectly! If you cannot fix this problem, delete the config.json file in your server directory", e);
            System.exit(0);
            throw new IllegalStateException("Unreachable code!");
        }
    }

    public int getMaxChunkLoadingDistance() {
        return maxChunkLoadingDistance;
    }

    /**
     * @return the json object of the servers default settings
     */
    @NotNull
    private JsonObject getDefaultConfigurationSettings() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("maxChunkLoadingDistance", DEFAULT_MAX_RENDER_DISTANCE);
        return jsonObject;
    }

    @NotNull
    public File getConfigurationFile() {
        return configurationFile;
    }
}
