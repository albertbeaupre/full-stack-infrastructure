package infrastructure.net.web.packets;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.WebServer;
import infrastructure.net.web.ui.UI;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class RoutePacketHandler implements PacketHandler {

    @Override
    public void handlePacket(SessionContext context, ByteBuf packet) {
        short length = packet.readShort();
        String path = packet.readCharSequence(length, Charset.defaultCharset()).toString();
        context.getUI().access(() -> {
            UI ui = context.getUI();
            ui.clear();

            WebServer.getRouter().handleRoute(path, ui);
        });
    }

}
