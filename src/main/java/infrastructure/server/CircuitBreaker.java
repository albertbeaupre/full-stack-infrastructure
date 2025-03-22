package infrastructure.server;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a circuit breaker pattern to manage server reliability and prevent
 * cascading failures in the load balancing system.
 *
 * <p>The CircuitBreaker tracks consecutive failures and opens the circuit when a
 * threshold is reached, preventing further requests until a reset timeout elapses.
 * This helps protect the system from overloading unhealthy servers.
 *
 * <p>Key features:
 * <ul>
 *     <li>Configurable failure threshold</li>
 *     <li>Automatic reset after timeout</li>
 *     <li>Thread-safe state management</li>
 * </ul>
 *
 * <p>Thread Safety: All methods are synchronized to ensure thread-safe state
 * transitions. The AtomicInteger provides concurrent failure counting.
 *
 * @author Albert Beaupre
 * @since March 13th, 2025
 */
public class CircuitBreaker {
    private static final Logger LOGGER = Logger.getLogger(CircuitBreaker.class.getName());
    private static final int FAILURE_THRESHOLD = 5;
    private static final long RESET_TIMEOUT_MS = 30_000; // 30 seconds

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;
    private volatile boolean isOpen = false;

    /**
     * Determines whether a request should be allowed to proceed to the associated server.
     *
     * <p>If the circuit is open, checks if the reset timeout has elapsed. If so, resets
     * the circuit and allows the request. If the circuit is closed, always allows the
     * request.
     *
     * @return true if the request is allowed, false if the circuit is open
     */
    public synchronized boolean allowRequest() {
        if (isOpen) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFailureTime > RESET_TIMEOUT_MS) {
                isOpen = false;
                failureCount.set(0);
                LOGGER.log(Level.INFO, "Circuit reset after timeout of {0}ms", RESET_TIMEOUT_MS);
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Records a successful request, resetting the failure count and closing the circuit.
     *
     * <p>This method is called when a request to the server completes successfully.
     */
    public synchronized void recordSuccess() {
        failureCount.set(0);
        isOpen = false;
        LOGGER.log(Level.INFO, "Circuit success recorded; failure count reset to 0");
    }

    /**
     * Records a failed request, potentially opening the circuit if the failure threshold
     * is reached.
     *
     * <p>Increments the failure count and opens the circuit if the threshold is met.
     * Updates the last failure timestamp when the circuit opens.
     */
    public synchronized void recordFailure() {
        int currentFailures = failureCount.incrementAndGet();
        if (currentFailures >= FAILURE_THRESHOLD) {
            isOpen = true;
            lastFailureTime = System.currentTimeMillis();
            LOGGER.log(Level.INFO, "Circuit opened due to {0} failures (threshold: {1})",
                    new Object[]{currentFailures, FAILURE_THRESHOLD});
        }
    }

    /**
     * Returns the current state of the circuit.
     *
     * @return true if the circuit is open, false if closed
     */
    public boolean isOpen() {
        return isOpen;
    }
}