package backend.pool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * The ObjectPool class manages a pool of objects of type T. It provides methods to borrow and return objects,
 * and ensures that the pool is thread-safe and that objects are properly managed.
 *
 * @author Albert Beaupre
 * @since August 29th, 2024
 * @param <T> The type of objects managed by the pool, which must extend PooledObject.
 */
public class ObjectPool<T extends PooledObject> {

    private final Queue<T> pool = new LinkedList<>();
    private final ObjectPoolFactory<T> factory;
    private final int maxSize;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    /**
     * Constructs a new ObjectPool with the specified factory, maximum size, and idle timeout.
     *
     * @param factory The factory used to create, validate, and destroy objects.
     * @param maxSize The maximum number of objects that the pool can hold.
     */
    public ObjectPool(ObjectPoolFactory<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    /**
     * Borrows an object from the pool. If no objects are available, it will either create a new one if the pool
     * has not reached its maximum size or wait until one becomes available.
     *
     * @return An object from the pool.
     * @throws RuntimeException If the thread is interrupted while waiting for an object.
     */
    public T borrow() {
        lock.lock();
        try {
            // Wait if the pool is empty and handle pool size limits
            while (pool.isEmpty()) {
                if (pool.size() < maxSize) { // This is because of the condition awaiting. It must be done
                    pool.offer(factory.create());
                } else {
                    try {
                        notEmpty.await(); // Wait for an object to be available
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            T obj = pool.poll();
            if (factory.validate(obj)) {
                return obj;
            } else {
                factory.destroy(obj);
                return borrow(); // Try to borrow another object
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an object to the pool. The object is reset to its initial state and made available for reuse.
     *
     * @param obj The object to be returned to the pool.
     */
    public void recycle(T obj) {
        lock.lock();
        try {
            obj.reset(); // Reset the object's state
            pool.offer(obj);
            notEmpty.signal(); // Notify waiting threads that an object is available
        } finally {
            lock.unlock();
        }
    }

    /**
     * Closes the pool and destroys all objects in it. This method should be called when the pool is no longer needed.
     */
    public void close() {
        lock.lock();
        try {
            while (!pool.isEmpty()) {
                T obj = pool.poll();
                factory.destroy(obj);
            }
        } finally {
            lock.unlock();
        }
    }
}
