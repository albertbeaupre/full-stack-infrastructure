package infrastructure.net.web;

import infrastructure.collections.queue.LongUUIDQueue;
import infrastructure.net.PacketHandler;
import infrastructure.net.web.packets.KeyPacketHandler;
import infrastructure.net.web.packets.MousePacketHandler;
import infrastructure.net.web.packets.RoutePacketHandler;
import infrastructure.net.web.packets.ValueChangePacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;

/**
 * Handles incoming WebSocket binary frames and routes them to appropriate packet handlers.
 * <p>
 * This class is part of the Netty channel pipeline and processes binary WebSocket frames
 * sent by clients. Each packet is expected to begin with a single byte representing a
 * {@code packetID}, which is then dispatched to a registered {@link PacketHandler}.
 * <p>
 * Upon WebSocket handshake, a new {@link SessionContext} is created and registered using
 * a unique session ID from a {@link LongUUIDQueue}. When a client disconnects,
 * the session is properly unregistered.
 * <p>
 * Supported packet IDs (via static initialization):
 * <ul>
 *   <li>0 → {@link RoutePacketHandler}</li>
 *   <li>1 → {@link MousePacketHandler}</li>
 *   <li>2, 3 → {@link KeyPacketHandler}</li>
 *   <li>4 → {@link ValueChangePacketHandler}</li>
 * </ul>
 *
 * @author Albert
 * @version 1.0
 * @since September 2024
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    /**
     * Logger used for debugging and error reporting.
     */
    private static final Logger Log = org.slf4j.LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * A thread-safe queue that generates long-based UUIDs used for session IDs.
     */
    private final LongUUIDQueue uuidQueue = new LongUUIDQueue();

    static {
        WebServer.registerHandler(0, new RoutePacketHandler());
        WebServer.registerHandler(1, new MousePacketHandler());
        WebServer.registerHandler(2, new KeyPacketHandler());
        WebServer.registerHandler(3, new KeyPacketHandler());
        WebServer.registerHandler(4, new ValueChangePacketHandler());
    }

    /**
     * Invoked when a client disconnects from the WebSocket.
     * Cleans up the associated session context.
     *
     * @param ctx the channel handler context
     * @throws Exception if cleanup fails
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        Log.debug("Channel closed: {}", ctx.channel().remoteAddress());

        SessionContext.unregister(ctx.channel());
    }

    /**
     * Processes an incoming binary WebSocket frame.
     * The first byte of the frame is used to determine the packet handler.
     *
     * @param ctx   the channel context
     * @param frame the binary WebSocket frame to process
     */
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
        SessionContext.set(context); // Ensure thread-local session context is set
        handler.handlePacket(context, buffer);
    }

    /**
     * Handles special Netty events, such as completion of the WebSocket handshake.
     * Upon handshake, a new session context is created and registered.
     *
     * @param ctx the channel context
     * @param evt the triggered event
     * @throws Exception if an error occurs while handling the event
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            long sessionID = uuidQueue.pop();
            SessionContext session = new SessionContext(sessionID, ctx.channel());
            SessionContext.register(ctx.channel(), session);
            SessionContext.set(session);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Handles uncaught exceptions on the channel and logs the error.
     *
     * @param ctx   the channel context
     * @param cause the throwable that caused the failure
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.error("Error on channel[{}]: {}", ctx.channel().remoteAddress(), cause.getMessage());
        cause.printStackTrace();
    }
}
