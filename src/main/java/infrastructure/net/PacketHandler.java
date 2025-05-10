package infrastructure.net;

import infrastructure.net.web.SessionContext;
import io.netty.buffer.ByteBuf;

public interface PacketHandler {

    void handlePacket(SessionContext context, ByteBuf packet);

}
