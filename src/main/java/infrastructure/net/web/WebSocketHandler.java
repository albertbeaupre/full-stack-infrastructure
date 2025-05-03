package infrastructure.net.web;

import infrastructure.collections.queue.LongUUIDQueue;
import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.Designer;
import infrastructure.net.web.ui.ValueComponent;
import infrastructure.net.web.ui.components.Button;
import infrastructure.net.web.ui.components.TextField;
import infrastructure.net.web.ui.components.VerticalLayout;
import infrastructure.net.web.ui.event.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.nio.charset.StandardCharsets;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final LongUUIDQueue uuidQueue = new LongUUIDQueue();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof BinaryWebSocketFrame binary) {
            ByteBuf buf = binary.content();
            if (!buf.isReadable()) return;

            int opcode = buf.readUnsignedByte();

            switch (opcode) {
                case 1 -> {
                    int componentID = buf.readInt();

                    SessionContext session = SessionContext.get();

                    if (session != null) {
                        Component component = session.getUI().get(componentID);
                        if (component == null) return;


                        int button = buf.readUnsignedByte();
                        int clientX = buf.readShort();
                        int clientY = buf.readShort();
                        int pageX = buf.readShort();
                        int pageY = buf.readShort();
                        int screenX = buf.readShort();
                        int screenY = buf.readShort();
                        int modifiers = buf.readUnsignedByte();

                        component.publish(new ClickEvent(
                                MouseButton.fromCode(button),
                                clientX, clientY,
                                pageX, pageY,
                                screenX, screenY,
                                modifiers
                        ));
                    }
                }
                case 2, 3 -> {
                    int componentID = buf.readInt();

                    SessionContext session = SessionContext.get();

                    if (session != null) {
                        Component component = session.getUI().get(componentID);
                        if (component == null) return;

                        boolean repeated = buf.readUnsignedByte() == 1;
                        int modifiers = buf.readUnsignedByte();
                        int keyLen = buf.readUnsignedShort();
                        byte[] keyBytes = new byte[keyLen];
                        buf.readBytes(keyBytes);
                        String keyStr = new String(keyBytes, StandardCharsets.UTF_8);
                        Key logicalKey = Key.fromKey(keyStr);

                        switch (opcode) {
                            case 2:
                                component.publish(new KeyUpEvent(logicalKey, repeated, modifiers));
                                break;
                            case 3:
                                component.publish(new KeyDownEvent(logicalKey, repeated, modifiers));
                                break;
                        }
                    }
                }
                case 4 -> {
                    int componentID = buf.readInt();

                    SessionContext session = SessionContext.get();
                    if (session == null) return;

                    if (session.getUI().get(componentID) instanceof ValueComponent component) {
                        int valueLen = buf.readUnsignedShort();
                        byte[] valueBytes = new byte[valueLen];
                        buf.readBytes(valueBytes);
                        component.publish(new ValueChangeEvent(component, component.getValue(), new String(valueBytes)));
                    }
                }
                case 0 -> {
                    int len = buf.readUnsignedShort();
                    byte[] routeBytes = new byte[len];
                    buf.readBytes(routeBytes);
                    String path = new String(routeBytes, StandardCharsets.UTF_8);

                    SessionContext session = SessionContext.get(ctx.channel());
                    if (session == null) return;

                    SessionContext.set(session);
                    /* TODO session.getUI().access(() -> {
                        session.getUI().getRouter().navigate(path);
                        session.getUI().push();
                    });*/
                }

                default -> System.err.println("[WebSocket] Unknown binary opcode: " + opcode);
            }
        } else {
            frame.release();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            long sessionID = uuidQueue.pop();
            SessionContext session = new SessionContext(sessionID, ctx.channel());
            SessionContext.register(ctx.channel(), session);
            SessionContext.set(session);

            session.getUI().add(Designer.button("Hey!").component());
            session.getUI().add(Designer.textField("Hey!").component());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}