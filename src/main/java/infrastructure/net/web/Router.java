package infrastructure.net.web;

import infrastructure.net.web.ui.DOMUpdate;
import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;
import infrastructure.net.web.ui.UI;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Router {

    private final UI ui;
    private final Map<String, Consumer<UI>> routes = new HashMap<>();

    public Router(UI ui) {
        this.ui = ui;
    }

    /**
     * Registers a route path and the UI logic to execute when navigated.
     *
     * @param path    The route path (e.g. "/", "/about")
     * @param handler A lambda that modifies the UI
     */
    public void route(String path, Consumer<UI> handler) {
        routes.put(path, handler);
    }

    /**
     * Navigates to a registered path, clearing current UI and applying updates.
     *
     * @param path The requested route (e.g. "/")
     */
    public void navigate(String path) {
        Consumer<UI> handler = routes.get(path);
        if (handler != null) {
            handler.accept(ui);
        } else {
            /* TODO ui.getDispatcher().queue(
                    new DOMUpdate(DOMUpdateType.SET_TEXT, 0)
                            .param(DOMUpdateParam.TEXT, "404 Not Found")
            );
            ui.push();*/
        }
    }

    public void pushRouteChange(String path) {
        if (ui == null || path == null) return;

        byte[] encoded = path.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.buffer(3 + encoded.length);

        buf.writeByte(0x03); // ROUTE_PUSH opcode (browser URL update)
        buf.writeShort(encoded.length);
        buf.writeBytes(encoded);

        // TODO ui.getSession().send(buf); // Sends to the client's WebSocket
    }


    /**
     * Exposes all registered routes for debugging or sitemap.
     */
    public Map<String, Consumer<UI>> getRoutes() {
        return routes;
    }
}
