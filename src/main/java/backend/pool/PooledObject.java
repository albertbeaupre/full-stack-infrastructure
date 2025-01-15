package backend.pool;

/**
 * The PooledObject interface defines the contract for objects that are managed by the object pool.
 *
 * <p>
 * Implementations of this interface must provide a method to reset the object's state.
 *
 * @author Albert Beaupre
 * @since August 29th, 2024
 */
public interface PooledObject {

    /**
     * Resets the object to its initial state.
     *
     * <p>
     * This method is called when an object is returned to the pool.
     */
    void reset();
}
