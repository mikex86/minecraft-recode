package me.gommeantilegit.craftingboard;

import me.gommeantilegit.craftingboard.api.ServerDataApi;
import me.gommeantilegit.craftingboard.data.IDataProvider;
import me.gommeantilegit.craftingboard.resource.ResourceServer;
import org.jetbrains.annotations.NotNull;
import spark.Route;

import java.net.MalformedURLException;
import java.net.URL;

import static spark.Spark.*;

/**
 * The endpoint serving the WebUI
 */
public class WebUI implements Runnable {

    /**
     * The prefix all classpath served resources start with
     */
    @NotNull
    private static final String SERVING_PREFIX = "webui/frontend";

    @NotNull
    private final ResourceServer resourceServer;

    /**
     * Provides the server data the WebUI exposes
     */
    @NotNull
    private final IDataProvider provider;

    /**
     * The port the web-ui should run on
     */
    private final int port;

    public WebUI(@NotNull IDataProvider provider, int port) {
        this.provider = provider;
        this.port = port;
        this.resourceServer = new ResourceServer(SERVING_PREFIX);
        Thread thread = new Thread(this, "WebUI-Thread");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        thread.start();
    }

    @NotNull
    public URL getURL() {
        try {
            return new URL("http://localhost:" + port);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        port(port);
        exposeGet("/", "view/index.html");
        exposeGet("/test", "view/test.html");
        exposeGet("lib/chartjs/Chart.js");
        exposeGet("lib/chartjs/Chart.min.js");
        exposeGet("lib/chartjs/Chart.css");
        exposeGet("lib/chartjs/Chart.min.css");
        exposeGet("lib/mdc/material-components-web.css");
        exposeGet("lib/mdc/material-components-web.min.css");
        exposeGet("lib/mdc/material-components-web.css.map");
        exposeGet("lib/mdc/material-components-web.min.css.map");

        exposeGet("lib/mdc/material-components-web.js");
        exposeGet("lib/mdc/material-components-web.min.js");
        exposeGet("lib/mdc/material-components-web.js.map");
        exposeGet("fonts/fontfaces.css");
        exposeGet("fonts/material_icons.woff2");
        exposeGet("fonts/Roboto-Regular.ttf");
        post("api", new ServerDataApi(provider));
    }

    @NotNull
    private Route serverClasspath(@NotNull String resource) {
        return this.resourceServer.serve(resource);
    }

    private void exposeGet(@NotNull String resourcePath) {
        get(resourcePath, this.resourceServer.serve(resourcePath));
    }

    private void exposeGet(@NotNull String httpRoutePath, @NotNull String resourcePath) {
        get(httpRoutePath, this.resourceServer.serve(resourcePath));
    }
}
