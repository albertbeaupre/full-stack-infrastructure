package infrastructure.net.web;

import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.components.H1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Router} class is responsible for managing navigation within an application.
 * It maintains a collection of routes, each bound to a specific path, and provides
 * methods for handling and managing navigation requests, updating the browser state,
 * and exposing registered routes for debugging or site mapping purposes.
 *
 * @author Albert Beaupre
 */
public class Router {

    /**
     * A mapping of route paths to their corresponding route definitions.
     */
    private final Map<String, Route> routes = new HashMap<>();

    /**
     * Represents the fallback route used when a navigation request targets an unknown
     * or non-registered path. This route is typically used to handle invalid navigation
     * attempts or provide a "404 Not Found" page within the application's user interface.
     * <p>
     * The {@code unknownRoute} is managed by the {@link Router} class and can be customized
     * to define the behavior or UI displayed when accessed. It ensures that unexpected
     * paths do not result in undefined behavior.
     */
    private Route unknownRoute;

    /**
     * Registers a route by associating its path with the route definition.
     * Allows the router to resolve and handle navigation requests based on the path.
     *
     * @param route The route to be registered, containing the path and associated logic.
     *              The path must be unique within the router and determines the route's destination.
     */
    public void addRoute(Route route) {
        routes.put(route.getPath(), route);
    }

    /**
     * Navigates to a registered path, clearing the current UI and applying updates.
     *
     * @param path The requested route (e.g. "/")
     */
    public void handleRoute(String path, UI ui) {
        Route route = this.routes.getOrDefault(path, unknownRoute);
        if (route == null) {
            ui.add(new H1("404 Unknown Route: " + path));
            return;
        }

        route.load(ui);
    }

    /**
     * Sends a navigation request to update the browser's URL to the provided path.
     * Encodes the path in UTF-8, prepares a binary payload, and transmits it
     * through the associated session's WebSocket channel.
     *
     * @param path The target route path to navigate to (e.g., "/home", "/about").
     *             Cannot be null; if null, the method returns without action.
     */
    protected void navigate(String path) {
        if (path == null)
            return;

        byte[] encoded = path.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.directBuffer(3 + encoded.length);

        buf.writeByte(0x03);
        buf.writeShort(encoded.length);
        buf.writeBytes(encoded);

        SessionContext.get().send(buf);
    }

    /**
     * Sets the unknown route for the router. The unknown route is used as a fallback
     * mechanism when navigation is requested to a path that does not match any
     * registered routes. This route typically displays a "not found" or
     * similar page to the user.
     *
     * @param route the {@link Route} instance to use as the unknown route.
     */
    public void setUnknownRoute(Route route) {
        this.unknownRoute = route;
    }

}
