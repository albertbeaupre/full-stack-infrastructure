package rsps;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * The {@code ResourceFileUtils} class provides utility methods for working with
 * local files and resources within the classpath. This includes retrieving files
 * or paths to resources and handling conversion between paths and {@link File} objects.
 *
 * <p>This class assumes that all resources are available in the classpath and should
 * be accessed using the system class loader. It provides methods to retrieve the
 * {@link File} or path for a resource specified by its relative path within the project.</p>
 */
public class IO {

    /**
     * Retrieves a {@link File} object corresponding to the given resource path.
     *
     * <p>This method locates a resource file in the classpath using the system class loader,
     * and converts it to a {@link File} object.</p>
     *
     * @param path the relative path to the resource within the classpath
     * @return a {@link File} object representing the resource
     * @throws NullPointerException if the resource path is null or the resource cannot be found
     * @throws RuntimeException if there is an error converting the resource path to a URI
     */
    public static File getLocalFile(String path) {
        try {
            if (path == null)
                throw new NullPointerException("Resource path cannot be null");
            URL resource = getLocalResource(path);
            Objects.requireNonNull(resource, "Resource not found");
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Unable to retrieve resource '%s' due to: %s", path, e.getMessage()));
        }
    }

    /**
     * Retrieves the local path of a resource as a {@link String}.
     *
     * <p>This method returns the file path of a resource in the classpath, which may be useful for
     * operations requiring the absolute path of the resource.</p>
     *
     * @param path the relative path to the resource within the classpath
     * @return the absolute path to the resource as a string
     * @throws NullPointerException if the resource path is null or the resource cannot be found
     */
    public static String getLocalPath(String path) {
        if (path == null)
            throw new NullPointerException("Resource path cannot be null");
        URL resource = getLocalResource(path);
        Objects.requireNonNull(resource, "Resource not found");
        return resource.getPath();
    }

    /**
     * Retrieves the {@link URL} of a resource from the classpath.
     *
     * <p>This method uses the system class loader to load a resource as a {@link URL}.
     * It is the underlying method used by {@link #getLocalFile(String)} and {@link #getLocalPath(String)}.</p>
     *
     * @param path the relative path to the resource within the classpath
     * @return a {@link URL} representing the resource, or null if not found
     */
    public static URL getLocalResource(String path) {
        return ClassLoader.getSystemClassLoader().getResource(path);
    }
}