package infrastructure.net.web.packets;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.Key;
import infrastructure.net.web.ui.event.KeyDownEvent;
import infrastructure.net.web.ui.event.KeyUpEvent;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class KeyPacketHandler implements PacketHandler {
    @Override
    public void handlePacket(SessionContext context, ByteBuf packet) {
        int packetID = packet.getByte(0);

        int componentID = packet.readInt();
        UI ui = context.getUI();
        Component component = ui.get(componentID);

        if (component == null)
            return;

        boolean repeated = packet.readUnsignedByte() == 1;
        int modifiers = packet.readUnsignedByte();
        int length = packet.readUnsignedShort();
        String keyName = packet.readCharSequence(length, StandardCharsets.UTF_8).toString();
        Key key = Key.fromName(keyName);

        switch (packetID) {
            case 2:
                component.publish(new KeyUpEvent(key, repeated, modifiers));
                break;
            case 3:
                component.publish(new KeyDownEvent(key, repeated, modifiers));
                break;
        }
    }
}
