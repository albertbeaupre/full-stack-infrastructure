package infrastructure.net.web.packets;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.components.Form;
import io.netty.buffer.ByteBuf;

public class SubmitPacketHandler implements PacketHandler {

    @Override
    public void handlePacket(SessionContext context, ByteBuf packet) {
        int componentID = packet.readInt();

        SessionContext session = SessionContext.get();
        if (session == null) return;
        UI ui = session.getUI();

        if (ui.get(componentID) instanceof Form form) {
            int valueLen = packet.readUnsignedShort();
            byte[] valueBytes = new byte[valueLen];
            packet.readBytes(valueBytes);

        }

    }

}
