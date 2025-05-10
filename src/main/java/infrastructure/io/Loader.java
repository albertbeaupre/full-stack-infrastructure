package infrastructure.io;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Functional interface for loading binary data from a file system path.
 *
 * <p>
 * This interface is annotated with {@link FunctionalInterface}, enabling
 * usage in lambda expressions and method references for concise loader definitions.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
@FunctionalInterface
public interface Loader {

    /**
     * Loads the contents of the file at the specified {@link Path} into a byte array.
     * <p>
     * Implementations should ensure that the entire file content is read and returned.
     * Depending on the use case, implementations may choose to buffer reads,
     * apply caching, perform validation, or transform the data before returning.
     *
     * @param path the file system {@link Path} pointing to the resource to load
     * @return a byte array containing the full contents of the file at {@code path}
     * @throws IOException if an I/O error occurs while reading the file,
     *                     such as if the file does not exist, is inaccessible,
     *                     or an underlying read operation fails
     */
    byte[] load(Path path) throws IOException;
}