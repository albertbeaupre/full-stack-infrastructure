package infrastructure.net.web.ui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single DOM update operation that will be sent to the client.
 * <p>
 * Each {@code DOMUpdate} encapsulates:
 * <ul>
 *   <li>a {@link DOMUpdateType} indicating the kind of DOM operation (e.g., append, remove, style change);</li>
 *   <li>a target component ID within the UI hierarchy;</li>
 *   <li>a set of typed parameters keyed by {@link DOMUpdateParam}, for efficient binary encoding.</li>
 * </ul>
 * <p>
 * Instances may be built up by chaining parameter additions via {@link #param} or bulk via {@link #params},
 * then serialized into a compact binary format with {@link #encode()} for transmission over a Netty channel.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class DOMUpdate {

    /**
     * Ordered map of update parameters. Uses a linked map to preserve insertion order,
     * which determines the sequence of fields in the binary encoding.
     */
    private final Map<DOMUpdateParam, Object> parameters = new LinkedHashMap<>();

    /**
     * Unique identifier of the target component for this update.
     */
    private final int componentID;

    /**
     * The type of DOM update (e.g., SET_TEXT, ADD_EVENT_LISTENER).
     */
    private final DOMUpdateType type;

    /**
     * Constructs a new {@code DOMUpdate} for the given type and component.
     *
     * @param type        the DOM update operation type
     * @param componentID the unique ID of the component to update
     */
    public DOMUpdate(DOMUpdateType type, int componentID) {
        this.componentID = componentID;
        this.type = type;
    }

    /**
     * Adds a single parameter to this update.
     * <p>
     * The parameter key’s integer code and the stringified value will be
     * written in the final binary encoding.
     *
     * @param param the parameter key (enum) to include
     * @param value the parameter value; will be converted to {@link String}
     * @return this {@code DOMUpdate} instance, for chaining
     */
    public DOMUpdate param(DOMUpdateParam param, Object value) {
        parameters.put(param, String.valueOf(value));
        return this;
    }

    /**
     * Adds multiple parameters at once to this update.
     * <p>
     * Existing parameters with the same keys will be overwritten.
     *
     * @param parameters a map of parameter keys to values
     * @return this {@code DOMUpdate} instance, for chaining
     */
    public DOMUpdate params(Map<DOMUpdateParam, Object> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    /**
     * Encodes this update into a Netty {@link ByteBuf} in a compact binary format.
     * <p>
     * Format:
     * <ol>
     *   <li>1 byte: {@link DOMUpdateType#getCode() update type code}</li>
     *   <li>4 bytes: component ID (big-endian int)</li>
     *   <li>1 byte: number of parameters</li>
     *   <li>For each parameter:</li>
     *   <ul>
     *     <li>1 byte: parameter ID ({@link DOMUpdateParam#getCode()})</li>
     *     <li>2 bytes: length of UTF-8 byte array (short)</li>
     *     <li>N bytes: UTF-8 bytes of the parameter value string</li>
     *   </ul>
     * </ol>
     *
     * @return a freshly allocated {@link ByteBuf} containing the encoded update
     */
    public ByteBuf encode() {
        ByteBuf buf = Unpooled.buffer();

        buf.writeByte(type.getCode()); // 1 byte: update type code
        buf.writeInt(componentID); // 4 bytes: target component ID
        buf.writeByte(parameters.size()); // 1 byte: parameter count

        for (Map.Entry<DOMUpdateParam, Object> entry : parameters.entrySet()) {
            byte[] valBytes = String.valueOf(entry.getValue()).getBytes(StandardCharsets.UTF_8);
            buf.writeByte(entry.getKey().getCode()); // 1 byte: param ID
            buf.writeShort(valBytes.length); // 2 bytes: length of value
            buf.writeBytes(valBytes); // N bytes: UTF-8 encoded value
        }

        return buf;
    }

    /**
     * Returns the component ID targeted by this update.
     *
     * @return the unique component identifier
     */
    public int getComponentID() {
        return componentID;
    }

    /**
     * Returns the type of this DOM update operation.
     *
     * @return the {@link DOMUpdateType} of this update
     */
    public DOMUpdateType getType() {
        return type;
    }
}