package infrastructure.database.chunk;

import infrastructure.io.ByteCodeStrategy;
import infrastructure.io.DynamicByteBuffer;
import infrastructure.io.RAFPoolFactory;
import infrastructure.io.RAFPoolObject;
import infrastructure.pool.ObjectPool;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A chunk-based, on-disk key-value store with variable-sized entries.
 * <p>
 * Data is stored in a single append-only data file ("data.bin"), with in-memory
 * pointers (segments) indicating each entry's start and end offsets. Keys and
 * pointers are persisted in a separate "keys.bin" file for recovery.
 * <p>
 * Features:
 * <ul>
 *   <li>Constant-time lookup by key via an in-memory {@link #segments} map</li>
 *   <li>Efficient pooling of {@link RandomAccessFile} handles for both key and data files</li>
 *   <li>Support for create, update (with chunk-shifting logic), retrieve, and delete operations</li>
 *   <li>Automatic directory and pointer-file initialization in the constructor</li>
 *   <li>Thread-safe {@code create} and {@code update} via synchronization</li>
 *   <li>Graceful cleanup of resources via {@link #close()}</li>
 * </ul>
 *
 * @param <K> the type of keys used to identify entries
 * @param <D> the type of data stored in each entry
 * @author Albert Beaupre
 * @version 1.0
 * @since May 10th, 2025
 */
public class VariableChunkDatabase<K, D> implements AutoCloseable {

    /**
     * Filename for the serialized key → pointer list.
     */
    private static final String KEY_FILE = "keys.bin";

    /**
     * Filename for the binary data chunks.
     */
    private static final String DATA_FILE = "data.bin";

    /**
     * In-memory map from keys to their data segment pointers.
     */
    private final HashMap<K, DataSegment> segments;

    /**
     * Pool of RandomAccessFile handles for reading/writing the key file.
     */
    private final ObjectPool<RAFPoolObject> keyPool;

    /**
     * Pool of RandomAccessFile handles for reading/writing the data file.
     */
    private final ObjectPool<RAFPoolObject> dataPool;

    /**
     * Strategy to serialize/deserialize keys to bytes.
     */
    private final ByteCodeStrategy<K> keyStrategy;

    /**
     * Strategy to serialize/deserialize data values to bytes.
     */
    private final ByteCodeStrategy<D> dataStrategy;

    /**
     * Initializes or recovers a VariableChunkDatabase in the given folder.
     * <p>
     * - Creates the directory if it does not exist.<br>
     * - Reads existing key pointers from {@value #KEY_FILE} (if present) to
     * populate the {@link #segments} map.<br>
     * - Initializes file pools for efficient RandomAccessFile reuse.
     *
     * @param folder       the directory path for storing database files
     * @param keyStrategy  strategy to convert keys to/from byte[]
     * @param dataStrategy strategy to convert data values to/from byte[]
     * @throws RuntimeException if a pointer file cannot be read or directories cannot be created
     */
    public VariableChunkDatabase(String folder, ByteCodeStrategy<K> keyStrategy, ByteCodeStrategy<D> dataStrategy) {
        this.keyStrategy = keyStrategy;
        this.dataStrategy = dataStrategy;
        this.segments = new HashMap<>();

        Path dir = Paths.get(folder);
        Path keyPath = dir.resolve(KEY_FILE);
        Path dataPath = dir.resolve(DATA_FILE);

        try {
            Files.createDirectories(dir);
            if (Files.exists(keyPath)) {
                DynamicByteBuffer buffer = new DynamicByteBuffer(Files.readAllBytes(keyPath));
                while (buffer.hasRemaining()) {
                    int keySize = buffer.readInt();
                    byte[] keyBytes = buffer.readBytes(keySize);
                    long pointer = buffer.readLong();
                    K key = keyStrategy.deconstruct(keyBytes);
                    segments.put(key, DataSegment.fromPointer(pointer));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize database at: " + folder, e);
        }

        this.keyPool = new ObjectPool<>(new RAFPoolFactory(keyPath.toString()), 200);
        this.dataPool = new ObjectPool<>(new RAFPoolFactory(dataPath.toString()), 200);
    }

    /**
     * Creates a new entry for the given key and data.
     * <p>
     * Writes data bytes to the end of the data file, records its segment,
     * and appends the key + pointer to the key file. Throws if key exists.
     *
     * @param key  non-null key to create
     * @param data non-null data to store
     * @throws NullPointerException          if {@code key} or {@code data} is null
     * @throws UnsupportedOperationException if the key already exists
     * @throws RuntimeException              on I/O errors during write
     */
    public synchronized void create(K key, D data) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
        if (segments.containsKey(key))
            throw new UnsupportedOperationException("Key already exists: " + key);

        RAFPoolObject keyObj = keyPool.borrow();
        RAFPoolObject dataObj = dataPool.borrow();
        try {
            byte[] keyBytes = keyStrategy.construct(key);
            byte[] dataBytes = dataStrategy.construct(data);

            RandomAccessFile dataRAF = dataObj.file();
            long start = dataRAF.length();
            long end = start + dataBytes.length;
            dataRAF.seek(start);
            dataRAF.write(dataBytes);

            DataSegment segment = new DataSegment((int) start, (int) end);
            segments.put(key, segment);

            RandomAccessFile keyRAF = keyObj.file();
            keyRAF.seek(keyRAF.length());
            keyRAF.writeInt(keyBytes.length);
            keyRAF.write(keyBytes);
            long pointer = (((long) segment.end) << 32) | (segment.start & 0xFFFFFFFFL);
            keyRAF.writeLong(pointer);
        } catch (IOException e) {
            throw new RuntimeException("Error creating entry for key: " + key, e);
        } finally {
            keyPool.recycle(keyObj);
            dataPool.recycle(dataObj);
        }
    }

    /**
     * Updates the data associated with the specified key. If the new data is of the same length
     * as the existing data, it is overwritten in place. Otherwise, the existing entry is deleted,
     * and the new data is appended to the data file. All following entries are shifted.
     *
     * @param key  the non-null key whose associated data is to be updated
     * @param data the non-null new data to replace the existing data
     * @throws NullPointerException     if {@code key} or {@code data} is null
     * @throws IllegalArgumentException if the specified {@code key} does not exist
     * @throws RuntimeException         if an I/O error occurs during the update operation
     */
    public synchronized void update(K key, D data) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");

        DataSegment segment = segments.get(key);
        if (segment == null)
            throw new IllegalArgumentException("Key not found: " + key);

        byte[] newBytes = dataStrategy.construct(data);
        RAFPoolObject dataObj = dataPool.borrow();
        try {
            RandomAccessFile raf = dataObj.file();
            if (newBytes.length == segment.length()) {
                raf.seek(segment.start);
                raf.write(newBytes);
            } else {
                int trailingLength = (int) (raf.length() - segment.end);
                byte[] trailing = new byte[trailingLength];
                raf.seek(segment.end);
                raf.readFully(trailing);

                raf.seek(segment.start);
                raf.write(trailing);

                raf.seek(trailingLength);
                raf.write(newBytes);

                for (Map.Entry<K, DataSegment> e : segments.entrySet()) {
                    if (e.getKey().equals(key)) continue;
                    DataSegment s = e.getValue();
                    if (s.start > segment.start) {
                        segments.put(e.getKey(), new DataSegment(s.start - segment.length(), s.end - segment.length()));
                    }
                }

                segments.put(key, new DataSegment(trailingLength, (int) raf.length()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating key: " + key, e);
        } finally {
            dataPool.recycle(dataObj);
        }
    }

    /**
     * Retrieves the data for the given key.
     *
     * @param key the key to look up
     * @return the stored data value, or {@code null} if the key is not present
     * @throws RuntimeException on I/O errors during read
     */
    public D retrieve(K key) {
        DataSegment seg = segments.get(key);
        if (seg == null) return null;

        RAFPoolObject dataObj = dataPool.borrow();
        try {
            RandomAccessFile raf = dataObj.file();
            byte[] bytes = new byte[seg.length()];
            raf.seek(seg.start);
            raf.readFully(bytes);
            return dataStrategy.deconstruct(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving key: " + key, e);
        } finally {
            dataPool.recycle(dataObj);
        }
    }

    /**
     * Deletes the entry for the given key and shifts any trailing data.
     *
     * @param key the key to delete
     * @throws NullPointerException if {@code key} is null
     * @throws RuntimeException     on I/O errors during read/write
     */
    public void delete(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        DataSegment segment = segments.get(key);
        if (segment == null) return;

        RAFPoolObject dataObj = dataPool.borrow();
        try {
            RandomAccessFile raf = dataObj.file();
            long fileLength = raf.length();
            int segmentLength = segment.length();

            int trailingLength = (int) (fileLength - segment.end);
            byte[] trailing = new byte[trailingLength];
            raf.seek(segment.end);
            raf.readFully(trailing);

            raf.seek(segment.start);
            raf.write(trailing);
            raf.setLength(fileLength - segmentLength);

            for (Map.Entry<K, DataSegment> e : segments.entrySet()) {
                if (e.getKey().equals(key)) continue;
                DataSegment s = e.getValue();
                if (s.start > segment.start) {
                    segments.put(e.getKey(), new DataSegment(s.start - segmentLength, s.end - segmentLength));
                }
            }

            segments.remove(key);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting key: " + key, e);
        } finally {
            dataPool.recycle(dataObj);
        }
    }

    /**
     * Writes the current in-memory key-pointer map back to disk and
     * releases file handles in both key and data pools.
     * <p>
     * The {@code keys.bin} file is cleared and rewritten from scratch.
     */
    @Override
    public void close() {
        RAFPoolObject keyObj = keyPool.borrow();
        try {
            RandomAccessFile keyRAF = keyObj.file();
            keyRAF.setLength(0);  // Clear existing contents
            for (Map.Entry<K, DataSegment> entry : segments.entrySet()) {
                K key = entry.getKey();
                DataSegment seg = entry.getValue();

                byte[] keyBytes = keyStrategy.construct(key);
                long pointer = (((long) seg.end) << 32) | (seg.start & 0xFFFFFFFFL);

                keyRAF.writeInt(keyBytes.length);
                keyRAF.write(keyBytes);
                keyRAF.writeLong(pointer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while writing key segments during close()", e);
        } finally {
            keyPool.recycle(keyObj);
            keyPool.close();
            dataPool.close();
        }
    }

    /**
     * Internal record representing the start (inclusive) and end (exclusive)
     * offsets of an entry within the data file.
     *
     * @param start the inclusive start offset
     * @param end   the exclusive end offset
     */
    private record DataSegment(int start, int end) {

        /**
         * Reconstructs a {@code DataSegment} from a packed 64-bit pointer
         * where the higher 32 bits store {@code end} and the lower 32 bits store {@code start}.
         *
         * @param pointer the 64-bit packed long
         * @return a {@code DataSegment} instance decoded from the pointer
         */
        public static DataSegment fromPointer(long pointer) {
            int s = (int) (pointer & 0xFFFFFFFFL);
            int e = (int) (pointer >>> 32);
            return new DataSegment(s, e);
        }

        /**
         * Returns the length of this segment in bytes.
         *
         * @return {@code end - start}
         */
        public int length() {
            return end - start;
        }
    }
}
