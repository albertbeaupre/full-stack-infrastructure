package infrastructure.database.chunk;

import infrastructure.collections.stack.LongFastStack;
import infrastructure.io.ByteCodeStrategy;
import infrastructure.io.RAFPoolFactory;
import infrastructure.io.RAFPoolObject;
import infrastructure.pool.ObjectPool;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The {@code ChunkDatabase} class provides a fixed-size, chunk-based key-value storage system
 * designed for efficient random-access operations on disk. It stores keys and their corresponding
 * data in separate binary files ({@code keys.bin} and {@code data.bin}), with each entry occupying
 * a fixed-size chunk. This implementation ensures thread-safety through key-specific locking and
 * optimizes resource usage with object pooling for file handles.
 *
 * <p>Data and keys are serialized into fixed-size chunks using a {@link ByteCodeStrategy} for
 * encoding and decoding. The database supports create, retrieve, update, and delete (CRUD)
 * operations, with unused chunk indices tracked for efficient space reuse. The class is designed
 * to be extended for specific key and data types through parameterized generics.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Thread-safe operations using per-key {@link ReentrantLock} instances.</li>
 *   <li>Efficient file handle management via {@link ObjectPool} for {@link RandomAccessFile}.</li>
 *   <li>Fixed-size chunk allocation for predictable storage and fast access.</li>
 *   <li>Support for pre-allocation of chunks and reuse of freed chunks.</li>
 *   <li>Automatic directory and file creation with error handling.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * // Define a concrete implementation with specific serialization strategies
 * ChunkDatabase<String, MyData> db = new ChunkDatabase<>(
 *     "dbFolder",
 *     new StringByteCodeStrategy(),  // Custom key serialization
 *     new MyDataByteCodeStrategy(), // Custom data serialization
 *     128,                          // Key chunk size
 *     1024,                         // Data chunk size
 *     1000                          // Pre-allocate 1000 chunks
 * );
 *
 * // Create a new entry
 * db.create("key1", new MyData());
 *
 * // Retrieve data
 * MyData data = db.retrieve("key1");
 * System.out.println("Retrieved: " + data);
 *
 * // Update data
 * db.update("key1", new MyData());
 *
 * // Delete entry
 * db.delete("key1");
 *
 * // Close database
 * db.close();
 * </pre>
 *
 * @param <K> The type of keys used to identify data entries.
 * @param <D> The type of data stored in each entry.
 * @author Albert
 * @version 1.1
 * @since August 31st, 2024
 */
public class ChunkDatabase<K, D> implements AutoCloseable {

    /**
     * The filename for storing unused chunk indices.
     */
    private static final String UNUSED_FILE = "unused.bin";

    /**
     * The filename for storing serialized keys and their pointers.
     */
    private static final String KEY_FILE = "keys.bin";

    /**
     * The filename for storing serialized data chunks.
     */
    private static final String DATA_FILE = "data.bin";

    /**
     * A map associating keys with their chunk indices (pointers) in the data file.
     * The pointer represents the chunk offset, not the byte offset.
     */
    private final HashMap<K, Long> pointers;

    /**
     * A thread-safe map storing per-key locks to ensure thread-safe operations.
     */
    private final ConcurrentHashMap<K, ReentrantLock> locks;

    /**
     * An object pool for managing {@link RandomAccessFile} instances for the key file.
     * Limits the number of open file handles and promotes reuse.
     */
    private final ObjectPool<RAFPoolObject> keyPool;

    /**
     * An object pool for managing {@link RandomAccessFile} instances for the data file.
     * Optimizes file access for read/write operations.
     */
    private final ObjectPool<RAFPoolObject> dataPool;

    /**
     * The strategy for serializing and deserializing keys.
     */
    private final ByteCodeStrategy<K> keyStrategy;

    /**
     * The strategy for serializing and deserializing data.
     */
    private final ByteCodeStrategy<D> dataStrategy;

    /**
     * The fixed size (in bytes) of each data chunk. Serialized data must not exceed this size.
     */
    private final int dataChunkSize;

    /**
     * The fixed size (in bytes) of each key chunk. Serialized keys must not exceed this size.
     */
    private final int keyChunkSize;

    /**
     * A pre-allocated zero-filled byte array for clearing or padding data chunks.
     */
    private final byte[] emptyData;

    /**
     * A pre-allocated zero-filled byte array for clearing or padding key chunks.
     */
    private final byte[] emptyKey;

    /**
     * A stack for tracking unused chunk indices, enabling efficient reuse of freed space.
     */
    private final LongFastStack unusedChunks;

    /**
     * The file system path to the database storage directory.
     */
    private final Path folder;

    /**
     * Constructs a new {@code ChunkDatabase} instance, initializing file pools, storage directories,
     * and loading existing key pointers.
     *
     * <p>The constructor creates the storage directory if it does not exist and initializes the
     * key and data files with pre-allocated chunks if specified. It also loads any existing unused
     * chunk indices and key pointers from disk. File access is managed through object pools to
     * optimize resource usage.</p>
     *
     * @param folder          The directory path for storing database files.
     * @param keyStrategy     The strategy for serializing/deserializing keys.
     * @param dataStrategy    The strategy for serializing/deserializing data.
     * @param keyChunkSize    The fixed size (in bytes) for each key chunk.
     * @param dataChunkSize   The fixed size (in bytes) for each data chunk.
     * @param chunkAllocation The number of chunks to pre-allocate if the database is new.
     * @throws RuntimeException If the storage directory cannot be created or if file operations fail.
     */
    public ChunkDatabase(String folder, ByteCodeStrategy<K> keyStrategy, ByteCodeStrategy<D> dataStrategy, int keyChunkSize, int dataChunkSize, int chunkAllocation) {
        this.keyStrategy = keyStrategy;
        this.dataStrategy = dataStrategy;
        this.dataChunkSize = dataChunkSize;
        this.keyChunkSize = keyChunkSize;
        this.pointers = new HashMap<>();
        this.locks = new ConcurrentHashMap<>();
        this.unusedChunks = new LongFastStack();
        this.emptyData = new byte[dataChunkSize];
        this.emptyKey = new byte[keyChunkSize];

        this.folder = Paths.get(folder);
        Path keyPath = this.folder.resolve(KEY_FILE);
        Path dataPath = this.folder.resolve(DATA_FILE);
        Path unusedPath = this.folder.resolve(UNUSED_FILE);

        try {
            // Ensure the storage directory exists
            Files.createDirectories(this.folder);

            // Initialize new database files with pre-allocated chunks if they don't exist
            if (!Files.exists(keyPath) && !Files.exists(dataPath) && chunkAllocation > 0) {
                Files.write(keyPath, new byte[keyChunkSize * chunkAllocation]);
                Files.write(dataPath, new byte[dataChunkSize * chunkAllocation]);
                for (int i = chunkAllocation; i > 0; i--)
                    this.unusedChunks.push(i - 1);
            }

            // Load unused chunk indices from file
            if (Files.exists(unusedPath)) {
                ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(unusedPath));
                while (buffer.remaining() >= Long.BYTES)
                    this.unusedChunks.push(buffer.getLong());
            } else {
                Files.createFile(unusedPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create chunk database directory: " + this.folder, e);
        }

        // Load existing key pointers from the key file
        if (Files.exists(keyPath)) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(keyPath));
                while (buffer.remaining() >= keyChunkSize) {
                    long pointer = buffer.position() / keyChunkSize;
                    byte[] data = new byte[keyChunkSize];
                    buffer.get(data);
                    K key = this.keyStrategy.deconstruct(data);
                    if (key != null) {
                        this.pointers.put(key, pointer);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot read chunk database pointer file: " + keyPath, e);
            }
        }

        // Initialize object pools for file access
        this.keyPool = new ObjectPool<>(new RAFPoolFactory(keyPath.toString()), 200);
        this.dataPool = new ObjectPool<>(new RAFPoolFactory(dataPath.toString()), 200);
    }

    /**
     * Creates a new entry in the database with the specified key and data.
     *
     * <p>The method ensures the key does not already exist, serializes the key and data, and writes
     * them to their respective files at a new or reused chunk index. Padding is applied to maintain
     * fixed-size chunks. Operations are thread-safe using a per-key lock.</p>
     *
     * @param key  The key for the new entry.
     * @param data The data to store.
     * @throws RuntimeException          If the key exists or an I/O error occurs.
     * @throws IndexOutOfBoundsException If the serialized key or data exceeds the chunk size.
     * @throws NullPointerException      If the key or data is null.
     */
    public void create(final K key, final D data) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(data, "Data cannot be written as null");

        if (pointers.containsKey(key))
            throw new RuntimeException("Key already exists: " + key);

        ReentrantLock lock = this.locks.computeIfAbsent(key, _ -> new ReentrantLock());
        lock.lock();

        RAFPoolObject keyObject = this.keyPool.borrow();
        RAFPoolObject dataObject = this.dataPool.borrow();
        RandomAccessFile keyRAF = keyObject.file();
        RandomAccessFile dataRAF = dataObject.file();
        try {

            // Serialize key and data
            byte[] keyData = this.keyStrategy.construct(key);
            if (keyData.length > this.keyChunkSize)
                throw new IndexOutOfBoundsException("Key chunk size must be <= the database key chunk size");

            byte[] construction = this.dataStrategy.construct(data);
            if (construction.length > this.dataChunkSize)
                throw new IndexOutOfBoundsException("Data chunk size must be <= the database data chunk size");

            // Determine chunk index (reuse unused or append)
            long pointer = this.unusedChunks.pop();
            long end = keyRAF.length() / keyChunkSize;

            if (pointer == -1)
                pointer = end;

            // Write key data
            keyRAF.seek(pointer * this.keyChunkSize);
            keyRAF.write(keyData);
            int remaining = this.keyChunkSize - keyData.length;
            if (remaining > 0 && pointer == end)
                keyRAF.write(new byte[remaining]);

            // Write data
            dataRAF.seek(pointer * this.dataChunkSize);
            dataRAF.write(construction);
            remaining = this.dataChunkSize - construction.length;
            if (remaining > 0 && pointer == end)
                dataRAF.write(new byte[remaining]);

            // Store pointer
            this.pointers.put(key, pointer);
        } catch (IOException e) {
            throw new RuntimeException("Error creating new chunk: " + e.getMessage(), e);
        } finally {
            this.keyPool.recycle(keyObject);
            this.dataPool.recycle(dataObject);
            lock.unlock();
        }
    }

    /**
     * Updates the data associated with an existing key.
     *
     * <p>The method verifies the key exists, serializes the new data, and overwrites the existing
     * data chunk. Padding ensures the chunk remains fixed-size. The operation is thread-safe using
     * a per-key lock.</p>
     *
     * @param key  The key whose data is to be updated.
     * @param data The new data.
     * @throws NullPointerException      If the key or data is null.
     * @throws IndexOutOfBoundsException If the serialized data exceeds the chunk size.
     * @throws RuntimeException          If an I/O error occurs.
     */
    public void update(final K key, final D data) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(data, "Data cannot be written as null");

        Long pointer = this.pointers.get(key);
        if (pointer == null)
            throw new NullPointerException("WTF");

        ReentrantLock lock = this.locks.computeIfAbsent(key, _ -> new ReentrantLock());
        lock.lock();

        RAFPoolObject dataObject = this.dataPool.borrow();
        RandomAccessFile dataRAF = dataObject.file();
        try {
            byte[] construction = this.dataStrategy.construct(data);

            dataRAF.seek(pointer * this.dataChunkSize);
            dataRAF.write(construction);
        } catch (IOException e) {
            throw new RuntimeException("Error updating chunk: " + e.getMessage(), e);
        } finally {
            this.dataPool.recycle(dataObject);
            lock.unlock();
        }
    }

    /**
     * Deletes the entry associated with the specified key.
     *
     * <p>The method marks the key and data chunks as deleted by overwriting them with zeros and
     * adds the chunk index to the unused stack. The key's pointer and lock are removed. The
     * operation is thread-safe.</p>
     *
     * @param key The key to delete.
     * @throws NullPointerException If the key is null.
     * @throws RuntimeException     If an I/O error occurs.
     */
    public void delete(final K key) {
        Objects.requireNonNull(key, "Cannot delete with null key");

        Long pointer = this.pointers.get(key);
        if (pointer == null)
            return;

        ReentrantLock lock = this.locks.computeIfAbsent(key, _ -> new ReentrantLock());
        lock.lock();

        RAFPoolObject keyObject = keyObject = this.keyPool.borrow();
        RAFPoolObject dataObject = dataObject = this.dataPool.borrow();
        try {
            this.pointers.remove(key);
            this.unusedChunks.push(pointer);

            RandomAccessFile keyRAF = keyObject.file();
            RandomAccessFile dataRAF = dataObject.file();

            keyRAF.seek(pointer * this.keyChunkSize);
            keyRAF.write(emptyKey);

            dataRAF.seek(pointer * this.dataChunkSize);
            dataRAF.write(emptyData);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete entry from database: " + e.getMessage(), e);
        } finally {
            if (keyObject != null) this.keyPool.recycle(keyObject);
            if (dataObject != null) this.dataPool.recycle(dataObject);
            lock.unlock();
            this.locks.remove(key);
        }
    }

    /**
     * Retrieves the data associated with the specified key.
     *
     * <p>The method reads the data chunk at the key's pointer and deserializes it using the
     * data strategy. Returns null if the key does not exist.</p>
     *
     * @param key The key whose data is to be retrieved.
     * @return The deserialized data, or null if the key is not found.
     * @throws NullPointerException If the key is null.
     * @throws RuntimeException     If an I/O error occurs.
     */
    public D retrieve(final K key) {
        Objects.requireNonNull(key, "Cannot retrieve with null key");

        Long pointer = this.pointers.get(key);
        if (pointer == null)
            return null;

        RAFPoolObject dataObject = this.dataPool.borrow();
        RandomAccessFile raf = dataObject.file();
        try {
            byte[] data = new byte[dataChunkSize];
            raf.seek(pointer * dataChunkSize);
            raf.read(data);
            return this.dataStrategy.deconstruct(data);
        } catch (IOException e) {
            throw new RuntimeException("Could not load from database: " + e.getMessage(), e);
        } finally {
            this.dataPool.recycle(dataObject);
        }
    }

    /**
     * Closes the database, saving unused chunk indices and releasing resources.
     *
     * <p>Writes the unused chunk indices to the unused file and closes the file pools.
     * Clears the pointers map to free memory.</p>
     *
     * @throws RuntimeException If an I/O error occurs while saving unused chunks.
     */
    @Override
    public void close() {
        try (FileChannel channel = FileChannel.open(this.folder.resolve(UNUSED_FILE),
                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * this.unusedChunks.size());
            for (Long unused : unusedChunks) {
                buffer.putLong(unused);
            }
            buffer.flip();
            channel.write(buffer);
            channel.force(true);
        } catch (IOException e) {
            throw new RuntimeException("Could not store unused chunks: " + e.getMessage(), e);
        }

        keyPool.close();
        dataPool.close();
        pointers.clear();
    }
}