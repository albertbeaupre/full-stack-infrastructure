package infrastructure.net.web.packets;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.ValueComponent;
import infrastructure.net.web.ui.event.ValueChangeEvent;
import io.netty.buffer.ByteBuf;

public class ValueChangePacketHandler implements PacketHandler {
    @Override
    public void handlePacket(SessionContext context, ByteBuf packet) {
        int componentID = packet.readInt();

        SessionContext session = SessionContext.get();
        if (session == null) return;
        UI ui = session.getUI();

        if (ui.get(componentID) instanceof ValueComponent component) {
            int valueLen = packet.readUnsignedShort();
            byte[] valueBytes = new byte[valueLen];
            packet.readBytes(valueBytes);
            component.publish(new ValueChangeEvent(component, component.construct(component.getValue()), new String(valueBytes)));
        }
    }
}
