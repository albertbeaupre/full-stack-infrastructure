package infrastructure.net.web;

import infrastructure.net.web.ui.UI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains context information for a single client session over WebSocket.
 * <p>
 * Each {@code SessionContext} holds:
 * <ul>
 *   <li>a unique session ID;</li>
 *   <li>a Netty {@link Channel} for sending BinaryWebSocketFrames;</li>
 *   <li>a {@link UI} instance representing the server-driven UI state.</li>
 * </ul>
 * <p>
 * Provides static methods to register/unregister sessions by session ID,
 * retrieve all active sessions, and manage a thread-local “current”
 * session for use in UI callbacks (e.g., {@code UI.access()}).
 * <p>
 * Also handles safe transmission of binary payloads via WebSocket frames,
 * including buffer lifecycle management to prevent memory leaks.
 */
public class SessionContext {

    /**
     * A {@code ThreadLocal} instance that maintains the current session context
     * associated with the executing thread. This allows thread-safe usage of session-specific
     * data during server-driven UI operations.
     */
    private static final ThreadLocal<SessionContext> CURRENT = new ThreadLocal<>();

    /**
     * Map of active sessions, keyed by their session ID.
     * Provides fast lookup for active WebSocket clients.
     */
    private static final Map<Long, SessionContext> SESSIONS = new ConcurrentHashMap<>();

    /**
     * Unique identifier assigned to this session.
     */
    private final long sessionID;

    /**
     * Netty channel used to send and receive WebSocket data.
     */
    private Channel channel;

    /**
     * UI instance associated with this session for rendering and event handling.
     */
    private final UI ui;

    /**
     * Constructs a new {@code SessionContext} with a given session ID and channel.
     * A new UI instance is also created for this session.
     *
     * @param sessionID the unique session ID
     * @param channel   the Netty channel connected to the client
     */
    public SessionContext(long sessionID, Channel channel) {
        this.sessionID = sessionID;
        this.channel = channel;
        this.ui = new UI();
    }

    /**
     * Registers the session instance for the given session ID.
     * Enables session lookup later via {@link #get(long)} or {@link #all()}.
     *
     * @param sessionID the session ID key
     * @param session   the {@code SessionContext} to associate
     */
    public static void register(long sessionID, SessionContext session) {
        SESSIONS.put(sessionID, session);
    }

    /**
     * Unregisters a session context for a given Netty channel.
     * <p>
     * This is a no-op here because session registration uses sessionID as key,
     * and this method has no effect unless changed to use sessionID instead of channel.
     *
     * @param channel the channel to unregister (unused in current implementation)
     */
    public static void unregister(Channel channel) {
        SESSIONS.remove(channel);
    }

    /**
     * Returns the session associated with the given ID.
     *
     * @param sessionID the ID of the session to look up.
     * @return the session context or {@code null} if not found
     */
    public static SessionContext get(long sessionID) {
        return SESSIONS.get(sessionID);
    }

    /**
     * Returns all active session contexts.
     *
     * @return map of session IDs to contexts
     */
    public static Map<Long, SessionContext> all() {
        return SESSIONS;
    }

    /**
     * Sets the current session context for this thread.
     *
     * @param session the session to associate with the current thread
     */
    public static void set(SessionContext session) {
        CURRENT.set(session);
    }

    /**
     * Retrieves the session context associated with the current thread.
     *
     * @return the thread-local session context or {@code null}
     */
    public static SessionContext get() {
        return CURRENT.get();
    }

    /**
     * Removes the session context from the current thread.
     * <p>
     * Useful after finishing session-scoped UI operations.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Sends a binary payload to the client via WebSocket.
     * If the channel is active, sends a duplicated buffer;
     * otherwise releases it to avoid memory leaks.
     *
     * @param buf the payload to send
     */
    public void send(ByteBuf buf) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new BinaryWebSocketFrame(buf.retainedDuplicate()));
        } else {
            buf.release();
        }
    }

    /**
     * Sets the channel associated with the session context.
     *
     * @param channel the Netty channel to associate with this session
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Gets the session ID.
     *
     * @return session ID
     */
    public long getSessionID() {
        return sessionID;
    }

    /**
     * Gets the channel associated with this session.
     *
     * @return Netty channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Gets the UI instance tied to this session.
     *
     * @return session UI
     */
    public UI getUI() {
        return ui;
    }
}
