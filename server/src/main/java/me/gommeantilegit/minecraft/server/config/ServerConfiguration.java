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
     * The default web-ui port to use
     */
    private static final int DEFAULT_WEB_UI_PORT = 37767;

    /**
     * The default number of idle ticks the server performs per second
     */
    private static final int DEFAULT_IDLE_TICKS = 100;

    /**
     * The file that stores the configuration content
     */
    @NotNull
    private static final File configurationFile = new File("config.json");

    /**
     * The gson instance
     */
    @NotNull
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The maximum render distance that a player is allowed to have
     */
    private final int maxChunkLoadingDistance;

    /**
     * The webui port
     */
    private final int webUIPort;

    /**
     * The amount of idle ticks per second should be performed to remain at a steady timer tick speed.
     * Increasing this value will reduce fluctuation of the tps value while most likely increasing cpu usage.
     */
    private final int idleTicks;

    public ServerConfiguration(int maxChunkLoadingDistance, int webUIPort, int idleTicks) {
        this.maxChunkLoadingDistance = maxChunkLoadingDistance;
        this.webUIPort = webUIPort;
        this.idleTicks = idleTicks;
    }

    public ServerConfiguration() {
        this(DEFAULT_MAX_RENDER_DISTANCE, DEFAULT_WEB_UI_PORT, DEFAULT_IDLE_TICKS);
    }

    @NotNull
    public static ServerConfiguration loadServerConfiguration(@NotNull ServerMinecraft mc) throws IOException {
        if (!configurationFile.exists()) {
            try {
                configurationFile.createNewFile();
            } catch (IOException e) {
                mc.getLogger().crash("Failed to create configuration file!", e);
            }
            JsonObject object = getDefaultConfigurationSettings();
            FileWriter writer = new FileWriter(configurationFile);
            writer.write(gson.toJson(object));
            writer.close();
        }
        try {
            return gson.fromJson(new BufferedReader(new FileReader(configurationFile)), ServerConfiguration.class);
        } catch (JsonSyntaxException | ClassCastException | NullPointerException e) {
            mc.getLogger().crash("Your json file is formatted incorrectly! If you cannot fix this problem, delete the config.json file in your server directory", e);
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
    private static JsonObject getDefaultConfigurationSettings() {
        return gson.toJsonTree(new ServerConfiguration()).getAsJsonObject();
    }

    @NotNull
    public File getConfigurationFile() {
        return configurationFile;
    }

    public int getWebUIPort() {
        return webUIPort;
    }

    public int getIdleTicks() {
        return idleTicks;
    }
}
