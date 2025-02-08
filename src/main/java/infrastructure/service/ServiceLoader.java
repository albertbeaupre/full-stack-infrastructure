package infrastructure.service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

/**
 * A generic service loader designed for dynamically loading and instantiating service classes
 * from a specified package. These service classes must either implement a given interface or
 * extend a specified base class. This flexible mechanism allows for the modular and dynamic
 * addition of services at runtime, which can significantly enhance the application's extensibility
 * and maintainability.
 * <p>
 * In addition to loading services, this class supports the use of a {@link ServiceInterceptor}
 * which provides a hook for custom logic to be executed immediately after a service instance is
 * created and before it is added to the collection of service instances. This interceptor can
 * perform tasks such as initialization, configuration, or validation of service instances,
 * allowing for more complex and dynamic service management.
 * <p>
 * Usage example:
 * <pre>
 * ServiceLoader<MyServiceInterface> loader = ServiceLoader.load("com.example.services", MyServiceInterface.class, service -> {
 *         // Perform interception logic, return true to accept the service, or false to reject it.
 *         return true;
 *     }
 * );
 * for (MyServiceInterface service : loader) {
 *     // Use the service
 * }
 * </pre>
 *
 * @param <T> The type parameter representing the interface or base class that the service classes
 *            should implement or extend. This ensures that all loaded services adhere to a
 *            consistent contract, facilitating their usage throughout the application.
 * @author Albert Beaupre
 * @version 1.0
 * @see ServiceInterceptor for details on intercepting and processing service instances.
 * @since 1.0
 */
public class ServiceLoader<T> implements Iterable<T> {

    private final List<T> serviceInstances = new ArrayList<>(); // Stores instances of dynamically loaded services.

    /**
     * Private constructor to prevent direct instantiation. ServiceLoader instances should be
     * obtained through the static {@link #load(String, Class, ServiceInterceptor)} method. This
     * design choice ensures that service loading and interception logic is encapsulated within
     * the loader, simplifying its usage.
     */
    private ServiceLoader() {
        // Prevent direct instantiation
    }

    /**
     * Loads service classes from the specified package that implement or extend the specified
     * service type and passes each instantiated service to a {@link ServiceInterceptor} for
     * potential processing. Only services approved by the interceptor are added to the loader's
     * collection.
     * <p>
     * This method scans the specified package for eligible classes, dynamically loads and
     * instantiates them, and then applies the interceptor logic to each instance. This approach
     * allows for dynamic discovery and configuration of services at runtime, enhancing the
     * application's flexibility.
     *
     * @param packageName The fully qualified name of the package to scan for eligible service classes.
     * @param type        The Class object representing the interface or base class that discovered
     *                    classes must implement/extend.
     * @param interceptor An instance of {@link ServiceInterceptor} that will be called for each
     *                    instantiated service, allowing for custom processing and filtering of services.
     * @param <T>         The type of the service interface or base class.
     * @return A ServiceLoader instance containing the loaded and intercepted service instances.
     * @throws NullPointerException if the specified resource does not exist or if the package
     *                              does not contain any files.
     */
    public static <T> ServiceLoader<T> load(String packageName, Class<T> type, ServiceInterceptor<T> interceptor) {
        ServiceLoader<T> services = new ServiceLoader<>();
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            URL resource = classLoader.getResource(packagePath);
            Objects.requireNonNull(resource, "Resource " + packagePath + " does not exist");
            File packageDirectory = new File(resource.toURI());
            if (packageDirectory.isDirectory()) {
                File[] files = packageDirectory.listFiles();
                Objects.requireNonNull(files, "Package does not contain any files");
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        if (type.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            Constructor<?> constructor = clazz.getDeclaredConstructor();
                            T instance = type.cast(constructor.newInstance());
                            // Apply the interceptor. If it returns true, add the instance to the collection.
                            if (interceptor.intercept(instance)) {
                                services.serviceInstances.add(instance);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return services;
    }

    /**
     * Returns the number of services that have been loaded and accepted by the interceptor.
     *
     * @return The number of loaded and intercepted service instances.
     */
    public int size() {
        return serviceInstances.size();
    }

    /**
     * Provides access to the loaded and intercepted service instances as an unmodifiable list.
     * This ensures the integrity of the service collection, preventing external modifications.
     *
     * @return An unmodifiable list of loaded and intercepted service instances.
     */
    public List<T> getServices() {
        return Collections.unmodifiableList(this.serviceInstances);
    }

    /**
     * Provides an iterator over the loaded and intercepted service instances, facilitating
     * iteration over the services in a for-each loop. This method enhances the usability of
     * the ServiceLoader, enabling straightforward access to and iteration over the services.
     *
     * @return An iterator for the loaded and intercepted service instances.
     */
    @Override
    public Iterator<T> iterator() {
        return serviceInstances.iterator();
    }
}