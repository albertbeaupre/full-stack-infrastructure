package infrastructure.web;

import infrastructure.web.ui.UI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionContext {

    private static final ThreadLocal<SessionContext> CURRENT = new ThreadLocal<>();
    private static final Map<Channel, SessionContext> SESSIONS = new ConcurrentHashMap<>();

    private final long sessionID;
    private final Channel channel;
    private final UI ui;

    public SessionContext(long sessionID, Channel channel) {
        this.sessionID = sessionID;
        this.channel = channel;
        this.ui = new UI();
    }

    /**
     * Associates a session with a channel.
     */
    public static void register(Channel channel, SessionContext session) {
        SESSIONS.put(channel, session);
    }

    /**
     * Removes a session associated with a channel (e.g. on disconnect).
     */
    public static void unregister(Channel channel) {
        SESSIONS.remove(channel);
    }

    /**
     * Returns the session associated with a channel.
     */
    public static SessionContext get(Channel channel) {
        return SESSIONS.get(channel);
    }

    /**
     * Returns all currently active sessions.
     */
    public static Map<Channel, SessionContext> all() {
        return SESSIONS;
    }

    /**
     * Sets the current thread-local session (used inside UI.access()).
     */
    public static void set(SessionContext session) {
        CURRENT.set(session);
    }

    /**
     * Gets the thread-local session.
     */
    public static SessionContext get() {
        return CURRENT.get();
    }

    /**
     * Clears the thread-local session reference.
     */
    public static void clear() {
        CURRENT.remove();
    }

    public void send(ByteBuf buf) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new BinaryWebSocketFrame(buf.retainedDuplicate()));
        } else {
            buf.release(); // prevent memory leak if channel is not active
        }
    }

    public long getSessionID() {
        return sessionID;
    }

    public Channel getChannel() {
        return channel;
    }

    public UI getUI() {
        return ui;
    }


}
