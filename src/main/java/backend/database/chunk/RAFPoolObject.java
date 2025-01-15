package backend.database.chunk;

import backend.pool.PooledObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The {@code RAFPoolObject} is a {@code PooledObject} implementation that wraps a {@code RandomAccessFile}.
 *
 * @author Albert Beaupre
 * @since August 31st, 2024
 */
public class RAFPoolObject implements PooledObject {

    private final RandomAccessFile file;

    /**
     * Constructs a new {@code RAFPoolObject} instance with the given {@code fileName}.
     *
     * <p>
     * The {@code fileName} is used to open a {@code RandomAccessFile} with the same name.
     *
     * @param fileName The name of the RandomAccessFile to open.
     */
    public RAFPoolObject(String fileName) {
        try {
            this.file = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        try {
            file.seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The {@code RandomAccessFile} this class wraps.
     */
    public RandomAccessFile file() {
        return file;
    }
}
