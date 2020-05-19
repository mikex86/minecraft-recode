package me.gommeantilegit.craftingboard.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import me.gommeantilegit.craftingboard.data.IDataProvider;
import org.jetbrains.annotations.NotNull;
import spark.Route;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ServerDataApi implements Route {

    @NotNull
    private final IDataProvider provider;

    public ServerDataApi(@NotNull IDataProvider provider) {
        this.provider = provider;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object handle(spark.Request request, spark.Response response) {
        String body = request.body();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ServerDataApi.Request parsedRequest = gson.fromJson(body, ServerDataApi.Request.class);
        long secondDif = parsedRequest.getEnd().getEpochSecond() - parsedRequest.getStart().getEpochSecond();
        if (secondDif < 0) {
            response.status(400);
            return "Bad Request";
        }
        List<Float> tps = this.provider.getData("server_tps", List.class, parsedRequest.getStart(), parsedRequest.getEnd());
        JsonArray playersJson = this.provider.getData("online_players", JsonArray.class);
        ApiPlayerData[] playerData = gson.fromJson(playersJson, ApiPlayerData[].class);
        response.type("application/json; charset=utf-8");
        return gson.toJson(new ServerDataApi.Response(
                Arrays.asList(playerData),
                tps
        ));
    }

    public static class Response {

        @NotNull
        @SerializedName("onlinePlayers")
        private final List<ApiPlayerData> onlinePlayers;

        @NotNull
        @SerializedName("tps")
        private final List<Float> tps;


        public Response(@NotNull List<ApiPlayerData> onlinePlayers, @NotNull List<Float> tps) {
            this.onlinePlayers = onlinePlayers;
            this.tps = tps;
        }

        @NotNull
        public List<Float> getTps() {
            return tps;
        }

        @NotNull
        public List<ApiPlayerData> getOnlinePlayers() {
            return onlinePlayers;
        }
    }

    public static class ApiPlayerData {

        @NotNull
        @SerializedName("name")
        private final String name;

        @SerializedName("playTime")
        private final long playTime;

        public ApiPlayerData(@NotNull String name, long playTime) {
            this.name = name;
            this.playTime = playTime;
        }

        public long getPlayTime() {
            return playTime;
        }

        @NotNull
        public String getName() {
            return name;
        }
    }

    public static class Request {

        @NotNull
        @SerializedName("range")
        private final long[] range;

        public Request(@NotNull long[] range) {
            if (range.length != 2) {
                throw new IllegalArgumentException("Request range array size not equal to 2!");
            }
            this.range = range;
        }

        @NotNull
        public Instant getStart() {
            return Instant.ofEpochMilli(this.range[0]);
        }

        @NotNull
        public Instant getEnd() {
            return Instant.ofEpochMilli(this.range[1]);
        }
    }
}
