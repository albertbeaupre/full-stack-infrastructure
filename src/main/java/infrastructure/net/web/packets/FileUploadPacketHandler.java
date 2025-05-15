package infrastructure.net.web.packets;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.ui.components.FileUploader;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;

public class FileUploadPacketHandler implements PacketHandler {

    private static final Logger Log = org.slf4j.LoggerFactory.getLogger(FileUploadPacketHandler.class);

    @Override
    public void handlePacket(SessionContext context, ByteBuf packet) {
        int componentID = packet.readInt();
        int fileNameLength = packet.readUnsignedShort();
        String fileName = packet.readCharSequence(fileNameLength, StandardCharsets.UTF_8).toString();

        int fileLength = packet.readInt();
        int available  = Math.min(packet.readableBytes(), fileLength);
        if (fileLength != available)
            Log.error("Possible file corruption: expected {}, got {}", fileLength, packet.readableBytes());


        if (context.getUI().get(componentID) instanceof FileUploader component) {
            byte[] data = new byte[available];
            packet.readBytes(data);
            component.getHandler().handle(fileName, data);
        }
    }

}
