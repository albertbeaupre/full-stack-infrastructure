package infrastructure.database.chunk;

import infrastructure.pool.ObjectPool;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The {@code ChunkDatabase} class provides a simple key-value storage mechanism
 * where data is stored in fixed-size chunks within binary files. It is designed
 * for efficient data retrieval and update operations, making it suitable for
 * use cases that require direct access to data stored on disk.
 *
 * <p>The database stores keys and data in separate binary files. The {@code keys.bin}
 * file holds the keys, while the {@code data.bin} file stores the corresponding data
 * chunks. The size of the chunks for both keys and data is configurable, providing
 * flexibility depending on the specific application requirements.</p>
 *
 * <p>The class supports asynchronous operations, leveraging a thread pool for executing
 * tasks. This allows for non-blocking interactions with the database, making it
 * well-suited for high-concurrency environments.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * ChunkDatabase<String, MyData> db = new MyChunkDatabaseImpl<>("dbFolder", 128, 1024, 4);
 *
 * db.create("key1", new MyData()).thenRun(() -> System.out.println("Data created!"));
 *
 * db.retrieve("key1").thenAccept(data -> System.out.println("Retrieved data: " + data));
 *
 * db.update("key1", new MyData()).thenRun(() -> System.out.println("Data updated!"));
 * }</pre>
 *
 * <p>In the example above, a database instance is created with specific chunk sizes for
 * keys and data. Data is created, retrieved, and updated asynchronously.</p>
 *
 * @param <K> The type of keys used by the database.
 * @param <D> The type of data stored in the database.
 * @author Albert Beaupre
 * @since August 31st, 2024
 */
public abstract class ChunkDatabase<K, D> {

    /**
     * The name of the file that stores the keys and their associated pointers.
     */
    private static final String KEY_FILE = "keys.bin";

    /**
     * The name of the file that stores the data chunks corresponding to the keys.
     */
    private static final String DATA_FILE = "data.bin";

    /**
     * A constant representing an invalid pointer value, used when a key does not exist.
     */
    private static final long INVALID_POINTER = -1;

    /**
     * A mapping of keys to their corresponding pointers in the database files. The pointer
     * indicates the position of the data associated with the key in the data file.
     */
    private final Map<K, Long> pointers;

    /**
     * A mapping of keys to their corresponding locks, ensuring thread-safe updates to data.
     */
    private final Map<K, Lock> locks;

    /**
     * A pool of random access file objects used for accessing and modifying the key file.
     */
    private final ObjectPool<RAFPoolObject> keyPool;

    /**
     * A pool of random access file objects used for accessing and modifying the data file.
     */
    private final ObjectPool<RAFPoolObject> dataPool;

    /**
     * The size of each chunk used to store data in the database.
     */
    private final int dataChunkSize;

    /**
     * The size of each chunk used to store keys in the database.
     */
    private final int keyChunkSize;

    /**
     * An executor service used for handling asynchronous database operations.
     */
    private final ExecutorService service;

    private final byte[] emptyData, emptyKey;

    /**
     * Constructs a new {@code ChunkDatabase} instance, initializing the key and data
     * file pools, and loading existing pointers from the key file if it exists.
     *
     * @param folder        The folder where the key and data files are stored.
     * @param keyChunkSize  The size of each key chunk in bytes.
     * @param dataChunkSize The size of each data chunk in bytes.
     * @param threadCount   The number of threads used for executing asynchronous tasks.
     */
    public ChunkDatabase(String folder, int keyChunkSize, int dataChunkSize, int threadCount) {
        this.dataChunkSize = dataChunkSize;
        this.keyChunkSize = keyChunkSize;
        this.pointers = new HashMap<>();
        this.locks = new ConcurrentHashMap<>();
        this.service = Executors.newFixedThreadPool(threadCount);
        this.keyPool = new ObjectPool<>(new RAFPoolFactory(folder.concat(File.separator).concat(KEY_FILE)), 200);
        this.dataPool = new ObjectPool<>(new RAFPoolFactory(folder.concat(File.separator).concat(DATA_FILE)), 200);
        this.emptyData = new byte[dataChunkSize];
        this.emptyKey = new byte[keyChunkSize];

        Path folderPath = Paths.get(folder);

        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create chunk database directory", e);
            }
        }

        String keyFilePath = folder + File.separator + KEY_FILE; // Use File.separator for OS compatibility
        Path path = Paths.get(keyFilePath);

        if (Files.exists(path)) {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                while (buffer.remaining() > 0) {
                    long pointer = buffer.position();
                    byte[] data = new byte[keyChunkSize];
                    buffer.get(data);
                    this.pointers.put(this.deconstructKey(data), pointer);
                }
                System.out.println(pointers);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read chunk database pointer file: " + keyFilePath, e);
            }
        }
    }

    /**
     * Converts a byte array into its corresponding data representation.
     * This method must be implemented by subclasses to define how data is
     * deserialized from its byte array form.
     *
     * @param data The byte array representing the data.
     * @return The deserialized data object.
     */
    protected abstract D deconstructData(byte[] data);

    /**
     * Converts a data object into its corresponding byte array representation.
     * This method must be implemented by subclasses to define how data is
     * serialized into a byte array.
     *
     * @param data The data object to serialize.
     * @return The byte array representing the serialized data.
     */
    protected abstract byte[] constructData(D data);

    /**
     * Converts a key into its corresponding byte array representation.
     * This method must be implemented by subclasses to define how keys are
     * serialized into a byte array.
     *
     * @param key The key to serialize.
     * @return The byte array representing the serialized key.
     */
    protected abstract byte[] constructKey(K key);

    /**
     * Converts a byte array into its corresponding key representation.
     * This method must be implemented by subclasses to define how keys are
     * deserialized from their byte array form.
     *
     * @param data The byte array representing the key.
     * @return The deserialized key object.
     */
    protected abstract K deconstructKey(byte[] data);

    /**
     * Creates a new entry in the database with the given key and data. This method
     * is asynchronous and ensures that no data corruption occurs by synchronizing
     * access to the relevant sections of the database files.
     *
     * <p>This method uses a {@link CompletableFuture} to handle the creation operation
     * asynchronously, allowing the caller to perform other tasks while the operation
     * is being processed.
     *
     * @param key  The key associated with the data.
     * @param data The data to store in the database.
     * @return A {@link CompletableFuture} representing the completion of the operation.
     * @throws IndexOutOfBoundsException If the key or data size exceeds the configured chunk size.
     */
    public CompletableFuture<Void> create(final K key, final D data) {
        return CompletableFuture.runAsync(() -> {
            if (pointers.containsKey(key))
                throw new RuntimeException("Key already exists: " + key);

            var keyObject = this.keyPool.borrow();
            var dataObject = this.dataPool.borrow();

            var keyRAF = keyObject.file();
            var dataRAF = dataObject.file();
            try {
                byte[] keyData = this.constructKey(key);

                if (keyData.length > this.keyChunkSize)
                    throw new IndexOutOfBoundsException("Key chunk size must be <= the database key chunk size");

                byte[] construction = this.constructData(data);

                if (construction.length > this.dataChunkSize)
                    throw new IndexOutOfBoundsException("Data chunk size must be <= the database data chunk size");

                var position = keyRAF.length();
                var pointer = position / this.keyChunkSize;

                // Write key values
                keyRAF.seek(position);
                keyRAF.write(keyData);
                keyRAF.write(new byte[this.keyChunkSize - (keyData.length % keyChunkSize)]);

                // Write data values
                dataRAF.seek(pointer * this.dataChunkSize);
                dataRAF.write(construction);
                dataRAF.write(new byte[this.dataChunkSize - (construction.length % dataChunkSize)]);

                this.pointers.put(key, pointer);
                this.keyPool.recycle(keyObject);
                this.dataPool.recycle(dataObject);
            } catch (IOException e) {
                throw new RuntimeException("Error creating new chunk: ".concat(e.getMessage()));
            }
        });
    }

    /**
     * Asynchronously updates the data associated with the given key in the database.
     * This method ensures thread-safe updates by acquiring a lock for the key being updated.
     *
     * @param key  The key whose data is to be updated.
     * @param data The new data to associate with the key.
     * @return A {@link CompletableFuture} representing the completion of the update operation.
     * @throws NullPointerException      If the key does not exist or if the data is null.
     * @throws IndexOutOfBoundsException If the data size exceeds the configured chunk size.
     */
    public CompletableFuture<Void> update(final K key, final D data) {
        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());

        return CompletableFuture.runAsync(() -> {
            Objects.requireNonNull(data, "Data cannot be written as null");

            final byte[] construction = this.constructData(data);

            if (construction.length > dataChunkSize)
                throw new IndexOutOfBoundsException("The data length must be <= " + this.dataChunkSize);

            lock.lock();
            try {
                long pointer = this.pointers.getOrDefault(key, INVALID_POINTER);

                if (pointer == INVALID_POINTER)
                    throw new NullPointerException("Key does not exist: " + key);

                var dataObject = this.dataPool.borrow();
                var raf = dataObject.file();
                try {
                    raf.seek(pointer * this.dataChunkSize);
                    raf.write(construction);
                    raf.write(new byte[this.dataChunkSize - (construction.length % this.dataChunkSize)]);
                } catch (IOException e) {
                    throw new RuntimeException("Could not write to database file: " + e.getMessage(), e);
                } finally {
                    this.dataPool.recycle(dataObject);
                }
            } finally {
                lock.unlock();
            }
        }, service);
    }

    /**
     * Asynchronously deletes the data associated with the given key from the database.
     * This method marks the entry as deleted and removes it from the pointers map.
     * The actual data space is not reclaimed, but the key is no longer accessible.
     *
     * @param key The key to delete from the database.
     * @return A {@link CompletableFuture} representing the completion of the delete operation.
     * @throws NullPointerException If the key does not exist in the database.
     */
    public CompletableFuture<Void> delete(final K key) {
        Lock lock = locks.computeIfAbsent(key, l -> new ReentrantLock());

        return CompletableFuture.runAsync(() -> {
            lock.lock();
            try {
                Long pointer = this.pointers.getOrDefault(key, INVALID_POINTER);

                if (pointer == INVALID_POINTER)
                    return;

                // Remove from pointers map
                this.pointers.remove(key);

                // Get file objects from pools
                var keyObject = this.keyPool.borrow();
                var dataObject = this.dataPool.borrow();

                try {
                    var keyRAF = keyObject.file();
                    var dataRAF = dataObject.file();

                    // Mark key as deleted by writing zeros to key chunk
                    keyRAF.seek(pointer * this.keyChunkSize);
                    keyRAF.write(emptyKey);

                    // Optionally mark data as deleted (write zeros)
                    dataRAF.seek(pointer * this.dataChunkSize);
                    dataRAF.write(emptyData);

                } catch (IOException e) {
                    throw new RuntimeException("Could not delete entry from database: " + e.getMessage(), e);
                } finally {
                    // Return objects to pools
                    this.keyPool.recycle(keyObject);
                    this.dataPool.recycle(dataObject);
                }

                // Remove lock after successful deletion
                locks.remove(key);

            } finally {
                lock.unlock();
            }
        }, service);
    }

    /**
     * Retrieves the data associated with the given key from the database asynchronously.
     * If the key does not exist, a {@link NullPointerException} is thrown.
     *
     * @param key The key whose associated data is to be retrieved.
     * @return A {@link CompletableFuture} containing the retrieved data.
     * @throws NullPointerException If the key does not exist in the database.
     */
    public CompletableFuture<D> retrieve(final K key) {
        return CompletableFuture.supplyAsync(() -> {
            long pointer = this.pointers.getOrDefault(key, INVALID_POINTER);

            if (pointer == INVALID_POINTER)
                return null;

            var dataObject = this.dataPool.borrow();
            var raf = dataObject.file();
            try {
                raf.seek(pointer * dataChunkSize);
                raf.read(emptyData);
                this.dataPool.recycle(dataObject);  // Place the random access file back into the pool

                return this.deconstructData(emptyData);
            } catch (IOException e) {
                throw new RuntimeException("Could not load from database: " + e.getMessage());
            }
        });
    }

    /**
     * Shuts down the ChunkDatabase, ensuring all resources are properly released.
     * This method waits for all pending tasks in the ExecutorService to complete,
     * with a configurable timeout, before closing the object pools.
     *
     * @param timeout  the maximum time to wait for tasks to complete
     * @param unit     the time unit of the timeout argument
     * @return true if shutdown completed successfully, false if it timed out or was interrupted
     */
    public boolean shutdown(long timeout, java.util.concurrent.TimeUnit unit) {
        boolean shutdownSuccessful = false;

        try {
            service.shutdown(); // Stops accepting new tasks

            // Wait for all tasks to finish up to the specified timeout
            if (!service.awaitTermination(timeout, unit)) {
                // If timeout occurs, forcefully terminate remaining tasks
                List<Runnable> unfinishedTasks = service.shutdownNow();
                System.err.println("Timeout occurred after " + timeout + " " + unit + ". Forcibly terminated " + unfinishedTasks.size() + " tasks.");
                // Partial success due to timeout
            } else {
                shutdownSuccessful = true; // Full success
            }

            // Step 2: Close the object pools after tasks are done
            keyPool.close();
            dataPool.close();

            // Step 3: Clear internal state (optional)
            pointers.clear();
            locks.clear();
        } catch (InterruptedException e) {
            // Handle interruption during shutdown
            service.shutdownNow(); // Force shutdown if interrupted
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            throw new RuntimeException("Failed to shut down ChunkDatabase properly", e);
        }

        return shutdownSuccessful;
    }

}