package infrastructure.net.web.packets;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.event.ClickEvent;
import infrastructure.net.web.ui.MouseButton;
import io.netty.buffer.ByteBuf;

public class MousePacketHandler implements PacketHandler {
    @Override
    public void handlePacket(SessionContext context, ByteBuf packet) {
        int componentID = packet.readInt();

        UI ui = context.getUI();
        Component component = ui.get(componentID);
        if (component == null)
            return;

        int button = packet.readUnsignedByte();
        int clientX = packet.readShort();
        int clientY = packet.readShort();
        int pageX = packet.readShort();
        int pageY = packet.readShort();
        int screenX = packet.readShort();
        int screenY = packet.readShort();
        int modifiers = packet.readUnsignedByte();

        component.publish(new ClickEvent(MouseButton.fromCode(button), clientX, clientY, pageX, pageY, screenX, screenY, modifiers));
    }
}