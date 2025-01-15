package backend.database.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code DocumentDatabase} represents a database that holds documents of 1 single type based on
 * the extension provided on construction of this class.
 *
 * <p>This class supports asynchronous creation,
 * appending, and retrieval of documents.
 *
 * @param <D> The document type.
 * @author Albert Beaupre
 * @since September 2nd, 2024
 */
public abstract class DocumentDatabase<D> {

    /**
     * The folder that holds all documents.
     */
    private final String folder;

    /**
     * The extension to append to every document.
     */
    private final String extension;

    /**
     * The service used to submit database tasks.
     */
    private final ExecutorService service;

    /**
     * Locks used to limit threads from creating/appending to documents.
     */
    private final ConcurrentHashMap<String, Lock> locks;

    /**
     * Constructs a new {@code DocumentDatabase} with the folder being the folder location, the extension being
     * the document extension type to append to the end of every file, and the threadCount being the amount of
     * threads usable for the internal {@code ExecutorService} to support asynchronous functionality.
     *
     * @param folder      The folder to hold the documents
     * @param extension   The extension to append to the end of every document
     * @param threadCount The threads usable for asynchronous functionality
     */
    public DocumentDatabase(String folder, String extension, int threadCount) {
        this.folder = folder;
        this.extension = extension;
        this.locks = new ConcurrentHashMap<>();
        this.service = Executors.newFixedThreadPool(threadCount);

        Path path = Paths.get(folder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path); // Create the folder if it doesn't exist
            } catch (Exception e) {
                throw new RuntimeException("Unable to create directory: ".concat(folder));
            }
        }
    }

    /**
     * Deconstructs a byte array into a document of type D.
     *
     * @param data the byte array representing the serialized document
     * @return the reconstructed document
     */
    public abstract D deconstruct(byte[] data);

    /**
     * Constructs a byte array from a document of type D.
     *
     * @param document the document to be serialized
     * @return the byte array representing the serialized document
     */
    public abstract byte[] construct(D document);

    /**
     * Creates a document with the given {@code key} as the name.
     *
     * @param key      The name to assign to the document
     * @param document The document to create.
     * @return The CompletableFuture<Void> that holds the functionality for creating the document
     */
    public CompletableFuture<Void> create(String key, D document) {
        return CompletableFuture.runAsync(() -> {
            Lock lock = locks.computeIfAbsent(key, v -> new ReentrantLock());
            try {
                lock.lock();
                Files.write(path(key), construct(document), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, service);
    }

    /**
     * Updates a document with the given {@code key} as the name.
     *
     * @param key      The name of the document to update.
     * @param document The document to update.
     * @return The CompletableFuture<Void> that holds the functionality for updating the document
     */
    public CompletableFuture<Void> update(String key, D document) {
        return CompletableFuture.runAsync(() -> {
            Lock lock = locks.computeIfAbsent(key, v -> new ReentrantLock());
            try {
                lock.lock();
                Files.write(path(key), construct(document), StandardOpenOption.WRITE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, service);
    }

    /**
     * Asynchronously appends the document to the existing document with name associated with the given
     * {@code key}.
     *
     * @param key      The name of the document
     * @param document The document to append to the current one
     * @return The CompletableFuture<Void> that holds the functionality for appending the document
     */
    public CompletableFuture<Void> append(String key, D document) {
        return CompletableFuture.runAsync(() -> {
            Lock lock = locks.computeIfAbsent(key, v -> new ReentrantLock());
            try {
                lock.lock();
                Files.write(path(key), construct(document), StandardOpenOption.APPEND);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, service);
    }

    /**
     * Asynchronously retrieves the document with the given {@code key}.
     *
     * @param key The name of the document
     * @return The CompletableFuture<D> that is supplied the document
     */
    public CompletableFuture<D> retrieve(String key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return deconstruct(Files.readAllBytes(path(key)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, service);
    }

    /**
     * @return The path constructed based on the given {@code name}, being the name of the document.
     */
    private Path path(String name) {
        return Paths.get(this.folder, name + '.' + this.extension);
    }
}
