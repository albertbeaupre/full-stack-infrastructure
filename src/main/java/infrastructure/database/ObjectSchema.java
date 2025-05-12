package infrastructure.database;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A schema describing how to serialize and deserialize objects of type {@code T} using {@link ByteBuffer}.
 * <p>
 * It contains metadata about field mappings, byte offsets, and conversion logic to/from binary format.
 *
 * @param <T> the target object type for serialization
 */
@SuppressWarnings("unchecked")
public class ObjectSchema<T> {

    /**
     * Default maximum character count for string serialization.
     */
    private static final int DEFAULT_MAX_CHARS = 120;

    /**
     * Default maximum element count for list/set serialization.
     */
    private static final int DEFAULT_MAX_ELEMENTS = 10;

    /**
     * Global cache for constructed schemas per class.
     */
    private static final Map<Class<?>, ObjectSchema<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * Ordered list of property mappers that know how to read/write fields of {@code T}.
     */
    private final List<PropertyMapper<T, ?>> mappers;

    /**
     * Total byte size required for serialized representation of {@code T}.
     */
    private final int totalSize;

    /**
     * Constructs an ObjectSchema with the provided mappers and size.
     */
    private ObjectSchema(List<PropertyMapper<T, ?>> mappers, int totalSize) {
        this.mappers = Collections.unmodifiableList(mappers);
        this.totalSize = totalSize;
    }

    /**
     * Creates a new schema builder for type {@code T}.
     *
     * @param <T> the object type to build the schema for
     * @return a new {@link Builder}
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder for defining and constructing an {@link ObjectSchema}.
     *
     * @param <T> the object type to be serialized
     */
    public static class Builder<T> {
        private final List<PropertyMapper<T, ?>> mappers = new ArrayList<>();
        private int cursor = 0;

        /**
         * Adds a byte property to the schema.
         */
        public Builder<T> addByte(Function<T, Byte> getter, BiConsumer<T, Byte> setter) {
            int size = Byte.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::put, (buf, off, obj) -> setter.accept(obj, buf.get(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds a short property to the schema.
         */
        public Builder<T> addShort(Function<T, Short> getter, BiConsumer<T, Short> setter) {
            int size = Short.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::putShort, (buf, off, obj) -> setter.accept(obj, buf.getShort(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds an int property to the schema.
         */
        public Builder<T> addInt(Function<T, Integer> getter, BiConsumer<T, Integer> setter) {
            int size = Integer.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::putInt, (buf, off, obj) -> setter.accept(obj, buf.getInt(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds a long property to the schema.
         */
        public Builder<T> addLong(Function<T, Long> getter, BiConsumer<T, Long> setter) {
            int size = Long.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::putLong, (buf, off, obj) -> setter.accept(obj, buf.getLong(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds a float property to the schema.
         */
        public Builder<T> addFloat(Function<T, Float> getter, BiConsumer<T, Float> setter) {
            int size = Float.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::putFloat, (buf, off, obj) -> setter.accept(obj, buf.getFloat(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds a double property to the schema.
         */
        public Builder<T> addDouble(Function<T, Double> getter, BiConsumer<T, Double> setter) {
            int size = Double.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::putDouble, (buf, off, obj) -> setter.accept(obj, buf.getDouble(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds a boolean property to the schema (stored as a single byte).
         */
        public Builder<T> addBoolean(Function<T, Boolean> getter, BiConsumer<T, Boolean> setter) {
            int size = 1;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> buf.put(off, (byte) (val ? 1 : 0)), (buf, off, obj) -> setter.accept(obj, buf.get(off) != 0)));
            cursor += size;
            return this;
        }

        /**
         * Adds a char property to the schema.
         */
        public Builder<T> addChar(Function<T, Character> getter, BiConsumer<T, Character> setter) {
            int size = Character.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, ByteBuffer::putChar, (buf, off, obj) -> setter.accept(obj, buf.getChar(off))));
            cursor += size;
            return this;
        }

        /**
         * Adds an enum property to the schema using ordinal-based serialization.
         */
        public <E extends Enum<E>> Builder<T> addEnum(Function<T, E> getter, BiConsumer<T, E> setter, Class<E> enumClass) {
            int size = Integer.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> buf.putInt(off, val.ordinal()), (buf, off, obj) -> {
                int ord = buf.getInt(off);
                setter.accept(obj, enumClass.getEnumConstants()[ord]);
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a fixed-length String property to the schema.
         *
         * @param maxChars maximum number of characters to store
         */
        public Builder<T> addString(Function<T, String> getter, BiConsumer<T, String> setter, int maxChars) {
            int size = maxChars * Character.BYTES;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> {
                char[] cs = val != null ? val.toCharArray() : new char[0];
                int len = Math.min(cs.length, maxChars);
                for (int i = 0; i < maxChars; i++) {
                    buf.putChar(off + i * Character.BYTES, i < len ? cs[i] : '\0');
                }
            }, (buf, off, obj) -> {
                char[] cs = new char[maxChars];
                for (int i = 0; i < maxChars; i++) {
                    cs[i] = buf.getChar(off + i * Character.BYTES);
                }
                int realLen = 0;
                while (realLen < maxChars && cs[realLen] != '\0') realLen++;
                setter.accept(obj, new String(cs, 0, realLen));
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a nested object to the schema using a provided {@link ObjectSchema}.
         */
        public <U> Builder<T> addObject(Function<T, U> getter, BiConsumer<T, U> setter, Supplier<U> factory, ObjectSchema<U> schema) {
            int size = schema.totalSize();
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> {
                for (PropertyMapper<U, ?> pm : schema.mappers()) {
                    pm.write(buf, off, val);
                }
            }, (buf, off, obj) -> {
                U nested = factory.get();
                for (PropertyMapper<U, ?> pm : schema.mappers()) {
                    pm.read(buf, off, nested);
                }
                setter.accept(obj, nested);
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a fixed-size list of objects to the schema.
         *
         * @param getter     function to get the list from the object
         * @param setter     consumer to set the list on the object
         * @param factory    supplier to instantiate list elements
         * @param elemSchema the object schema describing each list element
         * @param maxElems   the maximum number of elements to serialize
         * @param <U>        the type of the list elements
         * @return the builder instance
         */
        public <U> Builder<T> addList(Function<T, List<U>> getter, BiConsumer<T, List<U>> setter, Supplier<U> factory, ObjectSchema<U> elemSchema, int maxElems) {
            int lenBytes = Integer.BYTES;
            int elemSize = elemSchema.totalSize();
            int totalSize = lenBytes + maxElems * elemSize;

            mappers.add(new PropertyMapper<>(cursor, totalSize, getter, setter,

                    (buf, off, list) -> {
                        int n = list != null ? Math.min(list.size(), maxElems) : 0;
                        buf.putInt(off, n);
                        int base = off + lenBytes;
                        for (int i = 0; i < n; i++) {
                            U elem = list.get(i);
                            for (PropertyMapper<U, ?> pm : elemSchema.mappers()) {
                                pm.write(buf, base + i * elemSize, elem);
                            }
                        }
                    },

                    (buf, off, obj) -> {
                        int n = buf.getInt(off);
                        n = Math.min(n, maxElems);
                        List<U> list = new ArrayList<>(n);
                        int base = off + lenBytes;
                        for (int i = 0; i < n; i++) {
                            U elem = factory.get();
                            for (PropertyMapper<U, ?> pm : elemSchema.mappers()) {
                                pm.read(buf, base + i * elemSize, elem);
                            }
                            list.add(elem);
                        }
                        setter.accept(obj, list);
                    }));
            cursor += totalSize;
            return this;
        }

        /**
         * Adds a nullable field with a presence flag.
         *
         * @param getter   function to get the value from the object
         * @param setter   consumer to set the value on the object
         * @param baseSize the byte size of the actual field (excluding null flag)
         * @param writer   logic to write the value to the buffer
         * @param reader   logic to read the value from the buffer
         * @param <R>      the type of the field
         * @return the builder instance
         */
        public <R> Builder<T> addNullable(Function<T, R> getter, BiConsumer<T, R> setter, int baseSize, TriConsumer<ByteBuffer, Integer, R> writer, TriConsumer<ByteBuffer, Integer, T> reader) {
            int size = 1 + baseSize;  // 1 byte presence flag
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> {
                if (val == null) {
                    buf.put(off, (byte) 0);
                } else {
                    buf.put(off, (byte) 1);
                    writer.accept(buf, off + 1, val);
                }
            }, (buf, off, obj) -> {
                if (buf.get(off) == 0) {
                    setter.accept(obj, null);
                } else {
                    reader.accept(buf, off + 1, obj);
                }
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a fixed-length byte array to the schema.
         *
         * @param getter   function to get the byte array
         * @param setter   consumer to set the byte array
         * @param maxElems the fixed array length
         * @return the builder instance
         */
        public Builder<T> addByteArray(Function<T, byte[]> getter, BiConsumer<T, byte[]> setter, int maxElems) {
            int size = maxElems;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter,
                    // writer: pad/truncate to maxElems
                    (buf, off, arr) -> {
                        byte[] a = arr != null ? arr : new byte[0];
                        for (int i = 0; i < maxElems; i++) {
                            buf.put(off + i, i < a.length ? a[i] : 0);
                        }
                    },
                    // reader
                    (buf, off, obj) -> {
                        byte[] a = new byte[maxElems];
                        for (int i = 0; i < maxElems; i++) {
                            a[i] = buf.get(off + i);
                        }
                        setter.accept(obj, a);
                    }));
            cursor += size;
            return this;
        }

        /**
         * Adds a fixed-length int array to the schema.
         *
         * @param getter   function to get the int array
         * @param setter   consumer to set the int array
         * @param maxElems the maximum number of ints
         * @return the builder instance
         */
        public Builder<T> addIntArray(Function<T, int[]> getter, BiConsumer<T, int[]> setter, int maxElems) {
            int size = Integer.BYTES * maxElems;
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, arr) -> {
                int[] a = arr != null ? arr : new int[0];
                for (int i = 0; i < maxElems; i++) {
                    buf.putInt(off + i * Integer.BYTES, i < a.length ? a[i] : 0);
                }
            }, (buf, off, obj) -> {
                int[] a = new int[maxElems];
                for (int i = 0; i < maxElems; i++) {
                    a[i] = buf.getInt(off + i * Integer.BYTES);
                }
                setter.accept(obj, a);
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a binary blob (alias for {@link #addByteArray}) to the schema.
         *
         * @param getter   function to get the blob
         * @param setter   consumer to set the blob
         * @param maxBytes maximum number of bytes
         * @return the builder instance
         */
        public Builder<T> addBlob(Function<T, byte[]> getter, BiConsumer<T, byte[]> setter, int maxBytes) {
            return addByteArray(getter, setter, maxBytes);
        }

        /**
         * Adds a {@link LocalDate} field to the schema as epoch day (long).
         *
         * @param getter function to get the LocalDate
         * @param setter consumer to set the LocalDate
         * @return the builder instance
         */
        public Builder<T> addLocalDate(Function<T, LocalDate> getter, BiConsumer<T, LocalDate> setter) {
            int size = Long.BYTES;  // epoch-day
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> buf.putLong(off, val.toEpochDay()), (buf, off, obj) -> setter.accept(obj, LocalDate.ofEpochDay(buf.getLong(off)))));
            cursor += size;
            return this;
        }

        /**
         * Adds an {@link Instant} field to the schema using seconds and nanos.
         *
         * @param getter function to get the Instant
         * @param setter consumer to set the Instant
         * @return the builder instance
         */
        public Builder<T> addInstant(Function<T, Instant> getter, BiConsumer<T, Instant> setter) {
            int size = Long.BYTES + Integer.BYTES; // seconds + nanos
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> {
                buf.putLong(off, val.getEpochSecond());
                buf.putInt(off + Long.BYTES, val.getNano());
            }, (buf, off, obj) -> {
                long secs = buf.getLong(off);
                int nos = buf.getInt(off + Long.BYTES);
                setter.accept(obj, Instant.ofEpochSecond(secs, nos));
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a {@link Duration} field to the schema using seconds and nanos.
         *
         * @param getter function to get the Duration
         * @param setter consumer to set the Duration
         * @return the builder instance
         */
        public Builder<T> addDuration(Function<T, Duration> getter, BiConsumer<T, Duration> setter) {
            int size = Long.BYTES + Integer.BYTES; // seconds + nanos
            mappers.add(new PropertyMapper<>(cursor, size, getter, setter, (buf, off, val) -> {
                buf.putLong(off, val.getSeconds());
                buf.putInt(off + Long.BYTES, val.getNano());
            }, (buf, off, obj) -> {
                long secs = buf.getLong(off);
                int nos = buf.getInt(off + Long.BYTES);
                setter.accept(obj, Duration.ofSeconds(secs, nos));
            }));
            cursor += size;
            return this;
        }

        /**
         * Adds a fixed-size map of key-value pairs to the schema.
         *
         * @param getter     function to retrieve the map from the object
         * @param setter     consumer to set the map on the object
         * @param mapFactory supplier to create a new map instance
         * @param keyFactory supplier to create new key instances
         * @param keySchema  schema defining how to serialize/deserialize keys
         * @param valFactory supplier to create new value instances
         * @param valSchema  schema defining how to serialize/deserialize values
         * @param maxEntries maximum number of entries allowed in the serialized map
         * @param <K>        key type
         * @param <V>        value type
         * @return the current builder instance
         */
        public <K, V> Builder<T> addMap(Function<T, Map<K, V>> getter, BiConsumer<T, Map<K, V>> setter, Supplier<Map<K, V>> mapFactory, Supplier<K> keyFactory, ObjectSchema<K> keySchema, Supplier<V> valFactory, ObjectSchema<V> valSchema, int maxEntries) {
            int lenBytes = Integer.BYTES;
            int entrySize = keySchema.totalSize() + valSchema.totalSize();
            int totalSize = lenBytes + maxEntries * entrySize;

            mappers.add(new PropertyMapper<>(cursor, totalSize, getter, setter,

                    (buf, off, map) -> {
                        int n = map != null ? Math.min(map.size(), maxEntries) : 0;
                        buf.putInt(off, n);
                        int base = off + lenBytes;
                        int i = 0;
                        for (Map.Entry<K, V> e : map.entrySet()) {
                            if (i >= n) break;
                            K key = e.getKey();
                            V val = e.getValue();
                            for (var pm : keySchema.mappers())
                                pm.write(buf, base + i * entrySize, key);
                            for (var pm : valSchema.mappers())
                                pm.write(buf, base + i * entrySize + keySchema.totalSize(), val);
                            i++;
                        }
                    },

                    (buf, off, obj) -> {
                        int n = buf.getInt(off);
                        n = Math.min(n, maxEntries);
                        Map<K, V> m = mapFactory.get();
                        int base = off + lenBytes;
                        for (int i = 0; i < n; i++) {
                            K key = keyFactory.get();
                            for (var pm : keySchema.mappers())
                                pm.read(buf, base + i * entrySize, key);
                            V val = valFactory.get();
                            for (var pm : valSchema.mappers())
                                pm.read(buf, base + i * entrySize + keySchema.totalSize(), val);
                            m.put(key, val);
                        }
                        setter.accept(obj, m);
                    }));

            cursor += totalSize;
            return this;
        }


        /**
         * Adds a fixed-size set of elements to the schema.
         *
         * @param getter      function to retrieve the set from the object
         * @param setter      consumer to set the set on the object
         * @param setFactory  supplier to create a new set instance
         * @param elemFactory supplier to create new element instances
         * @param elemSchema  schema defining how to serialize/deserialize elements
         * @param maxEntries  maximum number of elements allowed in the serialized set
         * @param <E>         element type
         * @return the current builder instance
         */
        public <E> Builder<T> addSet(Function<T, Set<E>> getter, BiConsumer<T, Set<E>> setter, Supplier<Set<E>> setFactory, Supplier<E> elemFactory, ObjectSchema<E> elemSchema, int maxEntries) {
            int lenBytes = Integer.BYTES;
            int elemSize = elemSchema.totalSize();
            int totalSize = lenBytes + maxEntries * elemSize;

            mappers.add(new PropertyMapper<>(cursor, totalSize, getter, setter, (buf, off, set) -> {
                int n = set != null ? Math.min(set.size(), maxEntries) : 0;
                buf.putInt(off, n);
                int base = off + lenBytes;
                int i = 0;
                for (E elem : set) {
                    if (i >= n) break;
                    for (PropertyMapper<E, ?> pm : elemSchema.mappers()) {
                        pm.write(buf, base + i * elemSize, elem);
                    }
                    i++;
                }
            }, (buf, off, obj) -> {
                int n = buf.getInt(off);
                n = Math.min(n, maxEntries);
                Set<E> s = setFactory.get();
                int base = off + lenBytes;
                for (int i = 0; i < n; i++) {
                    E elem = elemFactory.get();
                    for (PropertyMapper<E, ?> pm : elemSchema.mappers()) {
                        pm.read(buf, base + i * elemSize, elem);
                    }
                    s.add(elem);
                }
                setter.accept(obj, s);
            }));

            cursor += totalSize;
            return this;
        }

        /**
         * Finalizes and builds the {@link ObjectSchema} from the configured mappings.
         *
         * @return a constructed {@link ObjectSchema} instance
         */
        public ObjectSchema<T> build() {
            return new ObjectSchema<>(mappers, cursor);
        }
    }

    /**
     * Returns the total number of bytes this schema will occupy in serialized form.
     *
     * @return total byte size
     */
    public int totalSize() {
        return totalSize;
    }

    /**
     * Returns the list of {@link PropertyMapper} instances that make up the schema.
     *
     * @return list of property mappers
     */
    public List<PropertyMapper<T, ?>> mappers() {
        return mappers;
    }

    /**
     * Builds or retrieves a cached {@link ObjectSchema} for the given class.
     * Uses a concurrent cache to avoid redundant schema generation.
     *
     * @param clazz the class to build schema for
     * @param <T>   the target type of the schema
     * @return a compiled {@link ObjectSchema} for the specified class
     */
    public static <T> ObjectSchema<T> buildSchema(Class<?> clazz) {
        return (ObjectSchema<T>) CACHE.computeIfAbsent(clazz, ObjectSchema::createSchema);
    }

    /**
     * Dynamically constructs an {@link ObjectSchema} based on the fields of the given class.
     * Handles primitive types, enums, strings, arrays, collections, maps, and nested objects.
     *
     * @param clazz the class to introspect and create schema for
     * @param <T>   the type represented by the schema
     * @return a fully constructed {@link ObjectSchema} instance
     */
    private static <T> ObjectSchema<T> createSchema(Class<?> clazz) {
        ObjectSchema.Builder<T> builder = ObjectSchema.builder();

        Field[] fields = clazz.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparingInt((Field f) -> {
            SchemaOrder sf = f.getAnnotation(SchemaOrder.class);
            return sf != null ? sf.order() : Integer.MAX_VALUE;
        }).thenComparing(Field::getName));

        for (Field f : fields) {
            f.setAccessible(true);
            Class<?> type = f.getType();

            Function<T, Object> getter = o -> {
                try {
                    return f.get(o);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
            BiConsumer<T, Object> setter = (o, v) -> {
                try {
                    f.set(o, v);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };

            SchemaLength lenAnn = f.getAnnotation(SchemaLength.class);
            int maxChars = (lenAnn != null && lenAnn.max() > 0) ? lenAnn.max() : DEFAULT_MAX_CHARS;
            int maxList = (lenAnn != null && lenAnn.max() > 0) ? lenAnn.max() : DEFAULT_MAX_ELEMENTS;

            if (type == byte.class || type == Byte.class) {
                builder.addByte(t -> (Byte) getter.apply(t), setter::accept);

            } else if (type == short.class || type == Short.class) {
                builder.addShort(t -> (Short) getter.apply(t), setter::accept);

            } else if (type == int.class || type == Integer.class) {
                builder.addInt(t -> (Integer) getter.apply(t), setter::accept);

            } else if (type == long.class || type == Long.class) {
                builder.addLong(t -> (Long) getter.apply(t), setter::accept);

            } else if (type == float.class || type == Float.class) {
                builder.addFloat(t -> (Float) getter.apply(t), setter::accept);

            } else if (type == double.class || type == Double.class) {
                builder.addDouble(t -> (Double) getter.apply(t), setter::accept);

            } else if (type == boolean.class || type == Boolean.class) {
                builder.addBoolean(t -> (Boolean) getter.apply(t), setter::accept);

            } else if (type == char.class || type == Character.class) {
                builder.addChar(t -> (Character) getter.apply(t), setter::accept);

            } else if (type == String.class) {
                builder.addString(t -> (String) getter.apply(t), setter::accept, maxChars);

            } else if (type.isEnum()) {
                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) type;
                Enum<?>[] constants = enumClass.getEnumConstants();
                builder.addInt(t -> ((Enum<?>) getter.apply(t)).ordinal(), (o, i) -> setter.accept(o, constants[i]));

            } else if (type == byte[].class) {
                builder.addBlob(t -> (byte[]) getter.apply(t), setter::accept, maxList);

            } else if (type == int[].class) {
                builder.addIntArray(t -> (int[]) getter.apply(t), setter::accept, maxList);

            } else if (type == LocalDate.class) {
                builder.addLocalDate(t -> (LocalDate) getter.apply(t), setter::accept);

            } else if (type == Instant.class) {
                builder.addInstant(t -> (Instant) getter.apply(t), setter::accept);

            } else if (type == Duration.class) {
                builder.addDuration(t -> (Duration) getter.apply(t), setter::accept);

            } else if (List.class.isAssignableFrom(type)) {
                Type gt = f.getGenericType();
                if (!(gt instanceof ParameterizedType pt))
                    throw new IllegalArgumentException("List missing generic type: " + f);
                Class<?> elemCls = (Class<?>) pt.getActualTypeArguments()[0];
                ObjectSchema<Object> elemSchema = buildSchema(elemCls);
                builder.addList(t -> (List<Object>) getter.apply(t), setter::accept, () -> instantiate(elemCls), elemSchema, maxList);

            } else if (Map.class.isAssignableFrom(type)) {
                Type gt = f.getGenericType();
                if (!(gt instanceof ParameterizedType pt))
                    throw new IllegalArgumentException("Map missing generic types: " + f);
                Class<?> keyCls = (Class<?>) pt.getActualTypeArguments()[0];
                Class<?> valCls = (Class<?>) pt.getActualTypeArguments()[1];
                ObjectSchema<Object> keySchema = buildSchema(keyCls);
                ObjectSchema<Object> valSchema = buildSchema(valCls);
                builder.addMap(t -> (Map<Object, Object>) getter.apply(t), setter::accept, HashMap::new, () -> instantiate(keyCls), keySchema, () -> instantiate(valCls), valSchema, maxList);

            } else if (Set.class.isAssignableFrom(type)) {
                Type gt = f.getGenericType();
                if (!(gt instanceof ParameterizedType pt))
                    throw new IllegalArgumentException("Set missing generic type: " + f);
                Class<?> elemCls = (Class<?>) pt.getActualTypeArguments()[0];
                ObjectSchema<Object> elemSchema = buildSchema(elemCls);
                builder.addSet(t -> (Set<Object>) getter.apply(t), setter::accept, HashSet::new, () -> instantiate(elemCls), elemSchema, maxList);

            } else {
                Class<Object> nestedCls = (Class<Object>) type;
                ObjectSchema<Object> nestedSchema = buildSchema(nestedCls);
                builder.addObject(t -> nestedCls.cast(getter.apply(t)), setter, () -> instantiate(nestedCls), nestedSchema);
            }
        }

        return builder.build();
    }

    /**
     * Utility method to instantiate a class using its no-arg constructor.
     *
     * @param cls the class to instantiate
     * @param <U> the type of object to instantiate
     * @return a new instance of the class
     * @throws RuntimeException if instantiation fails
     */
    private static <U> U instantiate(Class<U> cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
