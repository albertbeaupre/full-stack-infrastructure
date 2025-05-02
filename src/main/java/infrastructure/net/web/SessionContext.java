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
 * Provides static methods to register/unregister sessions by channel,
 * retrieve all active sessions, and manage a thread-local “current”
 * session for use in UI callbacks (e.g., {@code UI.access()}).
 * <p>
 * Also handles safe transmission of binary payloads via WebSocket frames,
 * including buffer lifecycle management to prevent memory leaks.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class SessionContext {

    /**
     * Thread-local holder for the session associated with the current thread.
     * <p>
     * Used by {@link UI#access()} and related mechanisms to route DOM updates
     * to the correct session without passing context explicitly.
     */
    private static final ThreadLocal<SessionContext> CURRENT = new ThreadLocal<>();

    /**
     * Global registry of all active sessions keyed by their Netty Channel.
     * <p>
     * Allows lookup of the {@code SessionContext} when messages arrive
     * on a channel. Uses {@link ConcurrentHashMap} for thread-safe access.
     */
    private static final Map<Channel, SessionContext> SESSIONS = new ConcurrentHashMap<>();

    /** Unique identifier for this session. */
    private final long sessionID;

    /** Netty channel over which to send BinaryWebSocketFrames for this session. */
    private final Channel channel;

    /** UI instance representing server-driven UI components for this session. */
    private final UI ui;

    /**
     * Creates a new session context.
     * <p>
     * Generates a fresh {@link UI} instance and associates the given
     * session ID and Netty channel.
     *
     * @param sessionID unique identifier for this session
     * @param channel   Netty channel used for WebSocket communication
     */
    public SessionContext(long sessionID, Channel channel) {
        this.sessionID = sessionID;
        this.channel = channel;
        this.ui = new UI();
    }

    /**
     * Registers a new session for the given channel.
     * <p>
     * Subsequent calls to {@link #get(Channel)} will return this session.
     *
     * @param channel the Netty channel to associate
     * @param session the session context to register
     */
    public static void register(Channel channel, SessionContext session) {
        SESSIONS.put(channel, session);
    }

    /**
     * Unregisters the session associated with the given channel.
     * <p>
     * Should be called on disconnect or channel inactive events
     * to free resources and avoid stale entries.
     *
     * @param channel the Netty channel whose session should be removed
     */
    public static void unregister(Channel channel) {
        SESSIONS.remove(channel);
    }

    /**
     * Looks up the session context for a given Netty channel.
     *
     * @param channel the channel on which the session is active
     * @return the registered {@code SessionContext}, or {@code null} if none
     */
    public static SessionContext get(Channel channel) {
        return SESSIONS.get(channel);
    }

    /**
     * Returns an unmodifiable view of all active sessions.
     * <p>
     * Modifications to the underlying map (via {@link #register} /
     * {@link #unregister}) are reflected in this view.
     *
     * @return map of active channels to their session contexts
     */
    public static Map<Channel, SessionContext> all() {
        return SESSIONS;
    }

    /**
     * Sets the thread-local current session context.
     * <p>
     * Used internally by UI framework when routing callbacks.
     *
     * @param session the session context to bind to the current thread
     */
    public static void set(SessionContext session) {
        CURRENT.set(session);
    }

    /**
     * Retrieves the thread-local current session context.
     *
     * @return the session context bound to this thread, or {@code null}
     */
    public static SessionContext get() {
        return CURRENT.get();
    }

    /**
     * Clears the thread-local current session reference.
     * <p>
     * Should be called after UI callbacks complete to prevent leakage
     * between threads or tasks.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Sends a binary payload to the client as a WebSocket frame.
     * <p>
     * Wraps the given {@link ByteBuf} in a {@link BinaryWebSocketFrame}
     * and writes it on the channel. If the channel is not active,
     * releases the buffer to avoid memory leaks.
     *
     * @param buf the Netty ByteBuf containing the payload to send
     */
    public void send(ByteBuf buf) {
        if (channel != null && channel.isActive()) {
            // Duplicate to allow buffer reuse by other handlers if needed
            channel.writeAndFlush(new BinaryWebSocketFrame(buf.retainedDuplicate()));
        } else {
            // Channel inactive: release buffer to prevent memory leak
            buf.release();
        }
    }

    /**
     * Returns the unique session ID.
     *
     * @return session identifier
     */
    public long getSessionID() {
        return sessionID;
    }

    /**
     * Returns the Netty channel for this session.
     *
     * @return the underlying Channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Returns the {@link UI} instance tied to this session.
     *
     * @return session-specific UI
     */
    public UI getUI() {
        return ui;
    }
}