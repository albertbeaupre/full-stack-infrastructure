package infrastructure.net.web;

import infrastructure.collections.queue.LongUUIDQueue;
import infrastructure.event.EventListener;
import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.Designer;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.ValueComponent;
import infrastructure.net.web.ui.components.Button;
import infrastructure.net.web.ui.components.Div;
import infrastructure.net.web.ui.components.TextField;
import infrastructure.net.web.ui.css.*;
import infrastructure.net.web.ui.event.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final LongUUIDQueue uuidQueue = new LongUUIDQueue();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        System.out.println("[WebSocket] Channel closed");

        SessionContext.unregister(ctx.channel());
    }

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
                        UI ui = session.getUI();
                        Component component = ui.get(componentID);
                        if (component == null) return;

                        int button = buf.readUnsignedByte();
                        int clientX = buf.readShort();
                        int clientY = buf.readShort();
                        int pageX = buf.readShort();
                        int pageY = buf.readShort();
                        int screenX = buf.readShort();
                        int screenY = buf.readShort();
                        int modifiers = buf.readUnsignedByte();

                        ui.publish(new ClickEvent(
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
                        UI ui = session.getUI();
                        Component component = ui.get(componentID);
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
                                ui.publish(new KeyUpEvent(logicalKey, repeated, modifiers));
                                break;
                            case 3:
                                ui.publish(new KeyDownEvent(logicalKey, repeated, modifiers));
                                break;
                        }
                    }
                }
                case 4 -> {
                    int componentID = buf.readInt();

                    SessionContext session = SessionContext.get();
                    if (session == null) return;
                    UI ui = session.getUI();

                    if (ui.get(componentID) instanceof ValueComponent component) {
                        int valueLen = buf.readUnsignedShort();
                        byte[] valueBytes = new byte[valueLen];
                        buf.readBytes(valueBytes);
                        ui.publish(new ValueChangeEvent(component, component.getValue(), new String(valueBytes)));
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
                    session.getUI().access(() -> {
                        UI ui = session.getUI();
                        ui.clear();

                        session.getUI().getRouter().handleRoute(path);
                    });
                }

                default -> System.err.println("[WebSocket] Unknown binary opcode: " + opcode);
            }
        } else {
            frame.release();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshake) {
            long sessionID = uuidQueue.pop();
            SessionContext session = new SessionContext(sessionID, ctx.channel());
            SessionContext.register(ctx.channel(), session);
            SessionContext.set(session);

            session.getUI().getRouter().route(new Route() {
                @Override
                public void load(UI ui) {
                    System.out.println("[WebSocket] Received route: " + handshake);
                    Div div = new Div();
                    Button button = new Button("TEST!");
                    div.getStyle().flexGrow(FlexGrow.GROW).flexDirection(FlexDirection.COLUMN).alignSelf(AlignSelf.CENTER).width("100%").height("100%").display(Display.FLEX).justifyContent(JustifyContent.CENTER).alignContent(AlignContent.CENTER);

                    TextField field = new TextField();

                    button.addClickListener(event -> field.setValue("" + System.currentTimeMillis()));

                    div.add(button);
                    div.add(field);
                    ui.add(div);
                }

                @Override
                public String getPath() {
                    return "/";
                }
            });
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