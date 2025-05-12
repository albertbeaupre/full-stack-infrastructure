package infrastructure.database;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Represents a fixed-size binary container for serializing and deserializing an object of type {@code T}
 * based on a given {@link ObjectSchema}. This class enables efficient mapping of structured object fields
 * to a byte array and vice versa.
 *
 * @param <T> the type of object this byte array structure is meant to serialize/deserialize
 */
public class ByteArray<T> {

    /** Backing byte array holding the serialized form of the object. */
    private final byte[] data;

    /** ByteBuffer wrapper around the backing array for structured access. */
    private final ByteBuffer buf;

    /** Object schema defining how object fields map to/from the byte buffer. */
    private final ObjectSchema<T> schema;

    /**
     * Constructs a new {@code ByteArray} with a new byte array of the appropriate size for the given schema.
     *
     * @param schema the object schema describing the structure and size
     */
    public ByteArray(ObjectSchema<T> schema) {
        this(schema, new byte[schema.totalSize()]);
    }

    /**
     * Constructs a {@code ByteArray} using an existing byte array and schema.
     *
     * @param schema   the object schema used for serialization/deserialization
     * @param existing the byte array that must exactly match the schema's total size
     * @throws IllegalArgumentException if the array length does not match the schema size
     */
    public ByteArray(ObjectSchema<T> schema, byte[] existing) {
        if (existing.length != schema.totalSize())
            throw new IllegalArgumentException("Byte array length " + existing.length + " does not match schema size " + schema.totalSize());

        this.schema = schema;
        this.data = existing;
        this.buf = ByteBuffer.wrap(data);
    }

    /**
     * Serializes the given object into the backing byte array using the schema's field mappers.
     *
     * @param obj the object to serialize
     */
    public void write(T obj) {
        buf.clear();
        List<PropertyMapper<T, ?>> mappers = schema.mappers();
        for (int i = 0, n = mappers.size(); i < n; i++) {
            mappers.get(i).write(buf, obj);
        }
    }

    /**
     * Deserializes the contents of the backing byte array into the given object using the schema's field mappers.
     *
     * @param obj the object to populate with values from the byte array
     */
    public void read(T obj) {
        buf.clear();
        List<PropertyMapper<T, ?>> mappers = schema.mappers();
        for (int i = 0, n = mappers.size(); i < n; i++) {
            mappers.get(i).read(buf, obj);
        }
    }

    /**
     * Returns the internal byte array storing the serialized data.
     *
     * @return the raw byte array backing this {@code ByteArray}
     */
    public byte[] getData() {
        return data;
    }
}