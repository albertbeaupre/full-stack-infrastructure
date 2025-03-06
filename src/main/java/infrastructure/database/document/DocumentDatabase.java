package infrastructure.database.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A high-performance, thread-safe document database for managing a single <b>type</b> of document.
 * Provides asynchronous operations for creating, updating, appending, and retrieving documents
 * with optimized concurrency control and minimal resource usage.
 *
 * <p>This class uses a fixed thread pool for asynchronous operations and employs a
 * lock-per-key strategy to ensure thread safety while maximizing parallelism.
 *
 * @param <D> The type of document managed by this database
 */
public abstract class DocumentDatabase<D> {
    private final Path folder;
    private final String extension;
    private final ExecutorService service;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Constructs a new DocumentDatabase instance.
     *
     * @param folder      The directory path where documents will be stored
     * @param extension   The file extension (without dot) to append to all document files
     * @param threadCount The number of threads in the pool for asynchronous operations
     * @throws RuntimeException if the folder cannot be created or accessed
     */
    public DocumentDatabase(String folder, String extension, int threadCount) {
        this.folder = Path.of(folder);
        this.extension = extension;
        // Create a fixed thread pool with daemon threads for better resource cleanup
        this.service = Executors.newFixedThreadPool(threadCount,
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                });

        try {
            // Ensure the storage directory exists, creating it if necessary
            Files.createDirectories(this.folder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + folder, e);
        }
    }

    /**
     * Deserializes a byte array into a document object.
     *
     * @param data The byte array containing the serialized document
     * @return The reconstructed document of type D
     */
    public abstract D deconstruct(byte[] data);

    /**
     * Serializes a document into a byte array for storage.
     *
     * @param document The document to serialize
     * @return The byte array representation of the document
     */
    public abstract byte[] construct(D document);

    /**
     * Asynchronously creates a new document with the specified key.
     *
     * <p>The document is written to a new file, failing if the file already exists.
     *
     * @param key      The unique identifier for the document
     * @param document The document to store
     * @return A CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> create(String key, D document) {
        return runAsync(key, () ->
                Files.write(path(key), construct(document), StandardOpenOption.CREATE_NEW));
    }

    /**
     * Asynchronously deletes a document by its key.
     *
     * <p>The document file is removed from the filesystem if it exists.
     *
     * @param key The identifier of the document to delete
     * @return A CompletableFuture that completes when the operation is done
     * @throws RuntimeException if the deletion fails
     */
    public CompletableFuture<Void> delete(String key) {
        return runAsync(key, () -> {
            Path filePath = path(key);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        });
    }

    /**
     * Asynchronously updates an existing document with the specified key.
     *
     * <p>The existing file is truncated and replaced with the new document content.
     *
     * @param key      The identifier of the document to update
     * @param document The new document content
     * @return A CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> update(String key, D document) {
        return runAsync(key, () ->
                Files.write(path(key), construct(document), StandardOpenOption.TRUNCATE_EXISTING));
    }

    /**
     * Asynchronously appends content to an existing document.
     *
     * <p>The new document data is added to the end of the existing file.
     *
     * @param key      The identifier of the document to append to
     * @param document The document content to append
     * @return A CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> append(String key, D document) {
        return runAsync(key, () ->
                Files.write(path(key), construct(document), StandardOpenOption.APPEND));
    }

    /**
     * Asynchronously retrieves a document by its key.
     *
     * @param key The identifier of the document to retrieve
     * @return A CompletableFuture supplying the retrieved document
     * @throws RuntimeException if the document cannot be read
     */
    public CompletableFuture<D> retrieve(String key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return deconstruct(Files.readAllBytes(path(key)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read document: " + key, e);
            }
        }, service);
    }

    /**
     * Executes an I/O operation asynchronously with lock protection.
     *
     * <p>Uses tryLock() for opportunistic locking and falls back to blocking lock
     * to minimize contention while ensuring thread safety.
     *
     * @param key The key identifying the document being operated on
     * @param op  The I/O operation to execute
     * @return A CompletableFuture that completes when the operation is done
     */
    private CompletableFuture<Void> runAsync(String key, IOOperation op) {
        return CompletableFuture.runAsync(() -> {
            ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
            if (lock.tryLock()) {
                try {
                    op.execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            } else {
                lock.lock();
                try {
                    op.execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }, service);
    }

    /**
     * Constructs the full file path for a document based on its key.
     *
     * @param name The document's identifier
     * @return The complete Path including folder, name, and extension
     */
    private Path path(String name) {
        return folder.resolve(name + "." + extension);
    }

    /**
     * Functional interface for I/O operations that may throw IOException.
     */
    @FunctionalInterface
    private interface IOOperation {

        /**
         * Executes the I/O operation.
         *
         * @throws IOException if the operation fails
         */
        void execute() throws IOException;
    }
}