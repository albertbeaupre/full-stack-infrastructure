package infrastructure.web.ui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a DOM update that can be encoded into a compact binary format.
 * Uses integer-based param keys for efficient encoding.
 */
public class DOMUpdate {

    private final Map<Integer, String> params = new LinkedHashMap<>();
    private final int componentID;
    private final DOMUpdateType type;

    public DOMUpdate(DOMUpdateType type, int componentID) {
        this.componentID = componentID;
        this.type = type;
    }

    public DOMUpdate param(DOMUpdateParam param, Object value) {
        params.put(param.ID(), String.valueOf(value));
        return this;
    }

    public DOMUpdate params(Map<Integer, String> paramMap) {
        params.putAll(paramMap);
        return this;
    }

    public ByteBuf encode() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(type.getCode());            // 1 byte: update type
        buf.writeInt(componentID);      // 4 bytes: node ID
        buf.writeByte(params.size());   // 1 byte: number of params

        for (Map.Entry<Integer, String> entry : params.entrySet()) {
            byte[] valBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
            buf.writeByte(entry.getKey());           // 1 byte: param ID
            buf.writeShort(valBytes.length);         // 2 bytes: value length
            buf.writeBytes(valBytes);                // N bytes: value
        }

        return buf;
    }

    public int getComponentID() {
        return componentID;
    }

    public DOMUpdateType getType() {
        return type;
    }
}