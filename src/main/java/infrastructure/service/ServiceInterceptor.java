package infrastructure.service;

/**
 * The ServiceInterceptor interface defines a single method for intercepting and handling services
 * of a specified type immediately after they are instantiated but before they are returned or used by the
 * ServiceLoader. This interception mechanism allows for additional processing, such as initialization,
 * configuration, or validation of service instances.
 * <p>
 * Implementations of this interface can modify, wrap, or replace service instances, offering a flexible
 * means to extend or customize the behavior of dynamically loaded services. This interface is particularly
 * useful for cross-cutting concerns that apply to all instances of a service type, such as logging,
 * security checks, or dependency injection.
 * <p>
 * Note: Implementors of this interface must ensure that the intercept method is thread-safe if the
 * ServiceLoader and services are intended to be used in a concurrent environment.
 *
 * @param <T> The type of the service interface or base class that the interceptor is concerned with. This
 *            ensures that the intercept method receives instances of the correct type.
 * @author Albert Beaupre
 * @since March 13th, 2025
 */
public interface ServiceInterceptor<T> {

    /**
     * Intercepts and processes a service instance immediately after its instantiation by the ServiceLoader.
     * This method provides an opportunity to perform additional operations on the service instance,
     * such as configuration, initialization, or applying cross-cutting concerns.
     * <p>
     * The implementation of this method can modify the passed service instance directly, wrap it with a
     * proxy to enhance its behavior, or perform any other necessary operations before the service instance
     * is used by the application.
     *
     * @param service The service instance to be intercepted. This instance has been freshly created
     *                by the ServiceLoader and not yet used or returned to the ServiceLoader's caller.
     * @return True if the service is eligible for the ServiceLoader; False otherwise.
     */
    boolean intercept(T service);

}