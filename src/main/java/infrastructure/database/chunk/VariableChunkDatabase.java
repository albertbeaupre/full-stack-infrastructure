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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @param <K> The type of keys used for identifying data entries.
 * @param <D> The type of the data stored in each entry.
 * @author Albert
 * @version 1.1
 * @since August 31st, 2024
 */
public class VariableChunkDatabase<K, D> implements AutoCloseable {

    /**
     * The filename used for storing keys and pointers.
     */
    private static final String KEY_FILE = "keys.bin";

    /**
     * The filename used for storing the actual data chunks.
     */
    private static final String DATA_FILE = "data.bin";

    /**
     * A map that maintains a relation between keys and their corresponding file chunk pointer.
     * The pointer is interpreted as the index (or offset in chunks) within the data file.
     */
    private final HashMap<K, DataSegment> segments;

    /**
     * A map the contains locks corresponding to keys within the database.
     */
    private final ConcurrentHashMap<K, ReentrantLock> locks;

    /**
     * An object pool managing {@link RandomAccessFile} objects for the key file.
     * This pool is used to efficiently handle multiple file accesses without creating too many handles.
     */
    private final ObjectPool<RAFPoolObject> keyPool;

    /**
     * An object pool managing {@link RandomAccessFile} objects for the data file.
     * It ensures efficient reuse for direct file access during read/write operations.
     */
    private final ObjectPool<RAFPoolObject> dataPool;

    /**
     * The path to the database folder.
     */
    private final Path folder;

    private final ByteCodeStrategy<K> keyStrategy;
    private final ByteCodeStrategy<D> dataStrategy;

    public VariableChunkDatabase(String folder, ByteCodeStrategy<K> keyStrategy, ByteCodeStrategy<D> dataStrategy) {
        this.keyStrategy = keyStrategy;
        this.dataStrategy = dataStrategy;
        this.segments = new HashMap<>();
        this.locks = new ConcurrentHashMap<>();

        this.folder = Paths.get(folder);
        Path keyPath = this.folder.resolve(KEY_FILE);
        Path dataPath = this.folder.resolve(DATA_FILE);

        if (Files.exists(keyPath)) {
            try {
                DynamicByteBuffer buffer = new DynamicByteBuffer(Files.readAllBytes(keyPath));

                while (buffer.hasRemaining()) {
                    int keySize = buffer.readInt();
                    byte[] data = buffer.readBytes(keySize);
                    long pointer = buffer.readLong();

                    K key = keyStrategy.deconstruct(data);
                    DataSegment segment = DataSegment.fromPointer(pointer);

                    this.segments.put(key, segment);
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot read chunk database pointer file: " + keyPath, e);
            }
        }

        this.keyPool = new ObjectPool<>(new RAFPoolFactory(keyPath.toString()), 200);
        this.dataPool = new ObjectPool<>(new RAFPoolFactory(dataPath.toString()), 200);
    }

    public void create(K key, D data) {
        if (this.segments.containsKey(key))
            throw new UnsupportedOperationException("Key " + key + " already exists");

        ReentrantLock lock = this.locks.computeIfAbsent(key, _ -> new ReentrantLock());

        lock.lock();

        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public D retrieve(K key) {
        DataSegment segment = this.segments.get(key);

        if (segment == null)
            return null;

        ReentrantLock lock = this.locks.computeIfAbsent(key, _ -> new ReentrantLock());

        lock.lock();

        try {
            var dataObject = this.dataPool.borrow();
            var dataRAF = dataObject.file();

            byte[] data = new byte[segment.length()];
            dataRAF.seek(segment.start);
            dataRAF.read(data);

            return this.dataStrategy.deconstruct(data);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve data: " + e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        keyPool.close();
        dataPool.close();
    }

    private record DataSegment(int start, int end) {
        public static DataSegment fromPointer(long pointer) {
            int start = (int) (pointer & 0xFFFFFFFFL);
            int end = (int) (pointer >> 32);
            return new DataSegment(start, end);
        }

        public int length() {
            return end - start;
        }
    }
}
