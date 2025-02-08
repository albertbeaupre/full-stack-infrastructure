package infrastructure.pool;

/**
 * The ObjectPoolFactory interface defines methods for creating, validating, and destroying objects.
 * Implementations of this interface are responsible for managing the lifecycle of the objects in the pool.
 *
 * @param <T> The type of objects managed by the factory, which must extend PooledObject.
 * @author Albert Beaupre
 * @since August 29th, 2024
 */
public interface ObjectPoolFactory<T extends PooledObject> {

    /**
     * Creates a new instance of an object to be managed by the pool.
     *
     * @return A new object instance.
     */
    T create();

    /**
     * Validates an object to ensure it is in a valid state before it is used.
     *
     * @param obj The object to be validated.
     * @return true if the object is valid, false otherwise.
     */
    boolean validate(T obj);

    /**
     * Destroys an object when it is no longer needed.
     *
     * @param obj The object to be destroyed.
     */
    void destroy(T obj);
}
