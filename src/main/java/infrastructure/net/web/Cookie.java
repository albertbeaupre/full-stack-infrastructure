package infrastructure.net.web;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for constructing WebSocket-compatible cookie packets for transmission
 * from server to client using a binary protocol format.
 * <p>
 * This class allows encoding standard cookie attributes (e.g. name, value, domain, path, etc.)
 * into a structured binary representation that can be sent through a WebSocket connection
 * instead of the traditional HTTP Set-Cookie header.
 * <p>
 * The binary format consists of a one-byte opcode followed by a count of key-value pairs,
 * each of which contains:
 * <ul>
 *   <li>1 byte key identifier</li>
 *   <li>2 bytes UTF-8 value length (unsigned short)</li>
 *   <li>UTF-8 encoded value bytes</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * ByteBuf encoded = new Cookie("session", "abc123")
 *     .httpOnly(true)
 *     .secure(true)
 *     .path("/")
 *     .encode();
 * }</pre>
 */
public final class Cookie {

    /**
     * Represents the opcode identifier for setting a cookie in a WebSocket communication.
     */
    private static final byte OPCODE_SET_COOKIE = 2;

    /**
     * Represents the key identifier used for specifying the "name" attribute of a cookie.
     */
    private static final byte KEY_NAME = 0;

    /**
     * Represents the key used to identify the value field in the cookie structure.
     */
    private static final byte KEY_VALUE = 1;

    /**
     * Represents the key identifier for the Path attribute in the cookie encoding process.
     */
    private static final byte KEY_PATH = 2;

    /**
     * Represents the byte identifier for the Max-Age attribute in cookie encoding.
     */
    private static final byte KEY_MAX_AGE = 3;

    /**
     * Represents a key identifier used for the Expires attribute in cookie encoding.
     */
    private static final byte KEY_EXPIRES = 4;

    /**
     * Represents a key identifier used for the Domain attribute in cookie encoding.
     */
    private static final byte KEY_DOMAIN = 5;

    /**
     * Represents a key identifier used for the secure attribute in cookie encoding.
     */
    private static final byte KEY_SECURE = 6;

    /**
     * Represents a key identifier used for the httpOnly attribute in cookie encoding.
     */
    private static final byte KEY_HTTP_ONLY = 7;

    /**
     * Represents a key identifier used for the sameSite attribute in cookie encoding.
     */
    private static final byte KEY_SAME_SITE = 8;

    private final String name;
    private final String value;
    private String path;
    private String maxAge;
    private String expires;
    private String domain;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;

    /**
     * Constructs a new Cookie with a given name and value.
     * <p>
     * All other optional fields are left unset until configured via method chaining.
     *
     * @param name  the name of the cookie (must not be null)
     * @param value the value of the cookie (must not be null)
     */
    public Cookie(String name, Object value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = String.valueOf(Objects.requireNonNull(value, "value"));
    }

    /**
     * Sets the Path attribute of the cookie.
     *
     * @param path path scope for which the cookie is valid
     * @return the updated {@code Cookie} instance
     */
    public Cookie path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the Max-Age attribute of the cookie.
     *
     * @param maxAge the maximum lifetime of the cookie in seconds
     * @return the updated {@code Cookie} instance
     */
    public Cookie maxAge(String maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the Expires attribute of the cookie.
     *
     * @param expires expiration timestamp as a string
     * @return the updated {@code Cookie} instance
     */
    public Cookie expires(String expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Sets the Domain attribute of the cookie.
     *
     * @param domain domain scope for which the cookie is valid
     * @return the updated {@code Cookie} instance
     */
    public Cookie domain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Enables or disables the Secure flag.
     * <p>
     * When true, the cookie will only be transmitted over HTTPS.
     *
     * @param secure true to enable the Secure attribute
     * @return the updated {@code Cookie} instance
     */
    public Cookie secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Enables or disables the HttpOnly flag.
     * <p>
     * When true, the cookie cannot be accessed via JavaScript.
     *
     * @param httpOnly true to enable the HttpOnly attribute
     * @return the updated {@code Cookie} instance
     */
    public Cookie httpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    /**
     * Sets the SameSite attribute of the cookie.
     *
     * @param sameSite one of {@code "Strict"}, {@code "Lax"}, or {@code "None"}
     * @return the updated {@code Cookie} instance
     */
    public Cookie sameSite(String sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    /**
     * Encodes the configured cookie attributes into a binary format suitable for WebSocket transmission.
     * <p>
     * The resulting buffer will start with an opcode, followed by the number of key-value pairs, then
     * each parameter encoded as:
     * <ul>
     *   <li>1 byte key</li>
     *   <li>2 bytes length (unsigned short)</li>
     *   <li>UTF-8 bytes</li>
     * </ul>
     *
     * @return a {@link ByteBuf} ready to send to the client
     */
    public ByteBuf encode() {
        Map<Byte, String> params = new LinkedHashMap<>();
        params.put(KEY_NAME, name);
        params.put(KEY_VALUE, value);
        if (path != null) params.put(KEY_PATH, path);
        if (maxAge != null) params.put(KEY_MAX_AGE, maxAge);
        if (expires != null) params.put(KEY_EXPIRES, expires);
        if (domain != null) params.put(KEY_DOMAIN, domain);
        params.put(KEY_SECURE, Boolean.toString(secure));
        params.put(KEY_HTTP_ONLY, Boolean.toString(httpOnly));
        if (sameSite != null) params.put(KEY_SAME_SITE, sameSite);

        int paramCount = params.size();

        // Calculate total size of encoded buffer
        int totalSize = 1 + 1; // opcode + count
        for (String v : params.values()) {
            int len = v.getBytes(StandardCharsets.UTF_8).length;
            totalSize += 1 + 2 + len; // key + length + data
        }

        ByteBuf buf = Unpooled.buffer(totalSize);
        buf.writeByte(OPCODE_SET_COOKIE).writeByte((byte) paramCount);

        // Write each parameter entry to the buffer
        for (Map.Entry<Byte, String> e : params.entrySet()) {
            byte key = e.getKey();
            byte[] data = e.getValue().getBytes(StandardCharsets.UTF_8);
            buf.writeByte(key).writeShort(data.length).writeBytes(data);
        }

        return buf;
    }
}