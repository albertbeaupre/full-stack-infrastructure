package backend.database.chunk;

import backend.pool.ObjectPoolFactory;

import java.io.IOException;

/**
 * The {@code RAFPoolFactory} is used to create, validate, or destroy {@code RAFPoolObject} instances.
 *
 * @author Albert Beaupre
 * @since August 31st, 2024
 */
public class RAFPoolFactory implements ObjectPoolFactory<RAFPoolObject> {

    private final String fileName;

    /**
     * Constructs a {@code RAFPoolFactory} instance with the given {@code fileName}.
     *
     * <p>
     * The {@code fileName} is used for constructing the {@code RandomAccessFile} within the
     * {@code RAFPoolObject}.
     *
     * @param fileName The name of the file to load for the RandomAccessFile.
     */
    public RAFPoolFactory(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public RAFPoolObject create() {
        return new RAFPoolObject(fileName);
    }

    @Override
    public boolean validate(RAFPoolObject obj) {
        try {
            return obj.file().getChannel().isOpen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy(RAFPoolObject obj) {
        try {
            obj.file().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
