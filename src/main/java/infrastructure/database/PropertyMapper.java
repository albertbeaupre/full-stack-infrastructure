package infrastructure.database;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a mapping between an object's property and its binary representation within a {@link ByteBuffer}.
 * Each {@code PropertyMapper} knows how to read and write a single property of type {@code R} on an object of type {@code T}.
 *
 * @param <T> the object type
 * @param <R> the field/property type
 */
public record PropertyMapper<T, R>(int offset, int length, Function<T, R> getter, BiConsumer<T, R> setter,
                                   TriConsumer<ByteBuffer, Integer, R> writer,
                                   TriConsumer<ByteBuffer, Integer, T> reader) {

    /**
     * Writes the mapped property from the object into the given {@link ByteBuffer} using a base offset of 0.
     *
     * @param buf the target buffer
     * @param obj the source object
     */
    public void write(ByteBuffer buf, T obj) {
        write(buf, 0, obj);
    }

    /**
     * Reads the mapped property from the given {@link ByteBuffer} into the object using a base offset of 0.
     *
     * @param buf the source buffer
     * @param obj the destination object
     */
    public void read(ByteBuffer buf, T obj) {
        read(buf, 0, obj);
    }

    /**
     * Writes the mapped property from the object into the {@link ByteBuffer}, using the specified base offset.
     *
     * @param buf        the target buffer
     * @param baseOffset the starting offset within the buffer
     * @param obj        the source object
     */
    public void write(ByteBuffer buf, int baseOffset, T obj) {
        R val = getter.apply(obj);
        writer.accept(buf, baseOffset + offset, val);
    }

    /**
     * Reads the mapped property from the {@link ByteBuffer} into the object, using the specified base offset.
     *
     * @param buf        the source buffer
     * @param baseOffset the starting offset within the buffer
     * @param obj        the destination object
     */
    public void read(ByteBuffer buf, int baseOffset, T obj) {
        reader.accept(buf, baseOffset + offset, obj);
    }
}
