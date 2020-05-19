package me.gommeantilegit.craftingboard.resource;

import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages and handles resource serving
 */
public class ResourceServer {

    /**
     * The class path directory where all resources that can be served are located
     */
    @NotNull
    private final String resourcePrefix;

    /**
     * A cache for resource contents
     */
    @NotNull
    private final Map<String, CachedResourceRoute> resourceCache = new ConcurrentHashMap<>();

    public ResourceServer(@NotNull String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    @NotNull
    public Route serve(@NotNull String resource) {
        return this.resourceCache.computeIfAbsent(resource, r -> {
            try {
                return new CachedResourceRoute(this.resourcePrefix + "/" + resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static class CachedResourceRoute implements Route {

        /**
         * The content of the resource to serve
         */
        @NotNull
        private final Object content;

        /**
         * The content type of the resource
         */
        @NotNull
        private final String contentType;

        @NotNull
        private final String resourceName;

        public CachedResourceRoute(@NotNull String resource) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resource), "Classpath resource " + resource + " not found!"), bos);

            this.resourceName = resource;
            String type = "text/plain; charset=utf-8";
            if (resource.endsWith(".html")) {
                type = "text/html; charset=utf-8";
            } else if (resource.endsWith(".map") || resource.endsWith(".json")) {
                type = "application/json; charset=utf-8";
            } else if (resource.endsWith(".js")) {
                type = "text/javascript; charset=utf-8";
            } else if (resource.endsWith(".css")) {
                type = "text/css; charset=utf-8";
            } else if (resource.endsWith(".woff2")) {
                type = "font/woff2";
            } else if (resource.endsWith(".ttf")) {
                type = "font/ttf";
            }
            this.content = type.startsWith("font") ? bos.toByteArray() : new String(bos.toByteArray(), StandardCharsets.UTF_8);
            this.contentType = type;
        }

        @Override
        public Object handle(Request request, Response response) {
            response.header("Content-Type", contentType);
            response.status(200);
            return content;
        }
    }
}
