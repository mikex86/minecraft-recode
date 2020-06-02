package me.gommeantilegit.minecraft.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gommeantilegit.craftingboard.data.IDataProvider;
import me.gommeantilegit.minecraft.ServerMinecraft;
import me.gommeantilegit.minecraft.entity.player.EntityPlayerMP;
import me.gommeantilegit.minecraft.server.netty.channel.ChannelData;
import me.gommeantilegit.minecraft.utils.data.DataCollector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * A data endpoint to expose server data
 */
public class ServerDataProvider implements IDataProvider {

    @NotNull
    private final ServerMinecraft minecraft;

    @NotNull
    private final Map<String, DataPort<?>> dataPortMap = new HashMap<>();

    public ServerDataProvider(@NotNull ServerMinecraft minecraft) {
        this.minecraft = minecraft;
    }

    {
        this.registerDataPort("server_tps", new ServerTpsDataPort());
        this.registerDataPort("online_players", new OnlinePlayersDataPort());
    }

    @NotNull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<DataPort<T>> getDataPort(@NotNull String dataPortName, @NotNull Class<T> type) {
        return Optional.ofNullable((DataPort) this.dataPortMap.get(dataPortName));
    }

    private <T> void registerDataPort(@NotNull String dataPortName, @NotNull DataPort<T> dataPort) {
        this.dataPortMap.put(dataPortName, dataPort);
    }

    /**
     * Data port for server TPS data.
     * Expects args[0] "start" and args[1] "end" as Instant which mark the time frame of data requested
     */
    private class ServerTpsDataPort implements IDataProvider.DataPort<List<Float>> {

        @NotNull
        @Override
        public List<Float> respondRequest(@NotNull Object... args) {
            DataCollector<Float> collector = minecraft.getTimer().getTpsDataCollector();
            Instant start = (Instant) args[0], end = (Instant) args[1];
            int endIndex = (int) (Instant.now().getEpochSecond() - start.getEpochSecond());
            int startIndex = (int) (Instant.now().getEpochSecond() - end.getEpochSecond());
            List<Float> data;
            try {
                data = collector.getData(startIndex, endIndex);
            } catch (IOException e) {
                throw new RuntimeException("Failed to retrieve data from data collector", e);
            }
            return data;
        }
    }

    /**
     * Data port for online players
     * Requires no arguments
     */
    private class OnlinePlayersDataPort implements IDataProvider.DataPort<JsonArray> {

        @NotNull
        @Override
        public JsonArray respondRequest(@NotNull Object... args) {
            JsonArray jsonArray = new JsonArray();
            Collection<ChannelData> playerData = minecraft.nettyServer.netHandlerPlayServer.getChannelData();
            for (ChannelData data : playerData) {
                EntityPlayerMP player = data.getPlayerMP();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", player.getUsername());
                jsonObject.addProperty("playTime", data.getTimeOnline());
                jsonArray.add(jsonObject);
            }
            return jsonArray;
        }
    }
}
