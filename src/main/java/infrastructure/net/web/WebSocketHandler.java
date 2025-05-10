package infrastructure.net.web;

import infrastructure.collections.queue.LongUUIDQueue;
import infrastructure.net.PacketHandler;
import infrastructure.net.web.packets.KeyPacketHandler;
import infrastructure.net.web.packets.MousePacketHandler;
import infrastructure.net.web.packets.RoutePacketHandler;
import infrastructure.net.web.packets.ValueChangePacketHandler;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.components.Button;
import infrastructure.net.web.ui.components.Div;
import infrastructure.net.web.ui.components.TextField;
import infrastructure.net.web.ui.css.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;

public class WebSocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private static final Logger Log = org.slf4j.LoggerFactory.getLogger(WebSocketHandler.class);
    private final LongUUIDQueue uuidQueue = new LongUUIDQueue();

    static {
        WebServer.registerHandler(0, new RoutePacketHandler());
        WebServer.registerHandler(1, new MousePacketHandler());
        WebServer.registerHandler(2, new KeyPacketHandler());
        WebServer.registerHandler(3, new KeyPacketHandler());
        WebServer.registerHandler(4, new ValueChangePacketHandler());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        Log.debug("Channel closed: {}", ctx.channel().remoteAddress());

        SessionContext.unregister(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        ByteBuf buffer = frame.content();
        if (!buffer.isReadable())
            return;

        int packetID = buffer.readUnsignedByte();

        PacketHandler handler = WebServer.getHandler(packetID);

        if (handler == null) {
            Log.debug("Unknown Packet ID: {}", packetID);
            return;
        }

        SessionContext context = SessionContext.get();
        SessionContext.set(context);
        handler.handlePacket(context, buffer);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshake) {
            long sessionID = uuidQueue.pop();
            SessionContext session = new SessionContext(sessionID, ctx.channel());
            SessionContext.register(ctx.channel(), session);
            SessionContext.set(session);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.error("Error on channel[{}]: {}", ctx.channel().remoteAddress(), cause.getMessage());
    }
}