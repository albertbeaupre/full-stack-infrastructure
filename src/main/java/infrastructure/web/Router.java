package infrastructure.web;

import infrastructure.web.ui.DOMUpdate;
import infrastructure.web.ui.DOMUpdateParam;
import infrastructure.web.ui.DOMUpdateType;
import infrastructure.web.ui.UI;
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
            pushRouteChange(path); // send 0x03 message to client
            handler.accept(ui);    // pass current UIContext
        } else {
            /* TODO ui.access(() -> {
                ui.getDispatcher().queue(
                        new DOMUpdate(DOMUpdateType.SET_TEXT, 0)
                                .param(DOMUpdateParam.TEXT, "404 Not Found")
                );
                ui.push();
            });*/
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
