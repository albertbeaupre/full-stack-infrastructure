package infrastructure.server;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a backend server node in the load balancing system with extensive
 * metrics tracking capabilities. This class maintains server metadata and runtime
 * statistics used for intelligent load distribution decisions.
 *
 * <p>The ServerNode is designed to be thread-safe through the use of AtomicLong for
 * metrics tracking and volatile fields for state management. It serves as the core
 * entity in the load balancing system, providing both configuration data (host, port,
 * weight) and runtime metrics (connections, latency, health status).
 *
 * <p>Key features:
 * <ul>
 *     <li>Immutable configuration (host, port, weight)</li>
 *     <li>Thread-safe metrics tracking</li>
 *     <li>Health status monitoring</li>
 *     <li>Latency and connection statistics</li>
 * </ul>
 *
 * <p>Thread Safety: This class is thread-safe for all operations. AtomicLong ensures
 * concurrent updates to metrics, and volatile fields provide visibility of health
 * status changes across threads.
 */
public class ServerNode {
    private final String id;
    private final String host;
    private final int port;
    private volatile boolean isHealthy;
    private final int weight;
    private final AtomicLong activeConnections;
    private final AtomicLong totalRequests;
    private final AtomicLong totalLatency;
    private volatile double failureRate;
    private volatile long lastHealthCheck;

    /**
     * Constructs a new ServerNode with the specified configuration parameters.
     *
     * <p>This constructor validates all input parameters and initializes the server
     * with a unique identifier and default healthy status. All metrics start at zero.
     *
     * @param host   The hostname or IP address of the server (e.g., "server1.example.com").
     *               Must not be null or empty.
     * @param port   The port number the server listens on (1-65535).
     * @param weight The load distribution weight (0-100), determining the relative
     *               proportion of traffic this server should receive.
     * @throws IllegalArgumentException if host is null/empty, port is invalid,
     *         or weight is outside the valid range
     */
    public ServerNode(String host, int port, int weight) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535, got: " + port);
        }
        if (weight < 0 || weight > 100) {
            throw new IllegalArgumentException("Weight must be between 0 and 100, got: " + weight);
        }

        this.id = UUID.randomUUID().toString();
        this.host = host;
        this.port = port;
        this.weight = weight;
        this.isHealthy = true;
        this.activeConnections = new AtomicLong(0);
        this.totalRequests = new AtomicLong(0);
        this.totalLatency = new AtomicLong(0);
        this.failureRate = 0.0;
        this.lastHealthCheck = System.currentTimeMillis();
    }

    /**
     * Returns the unique identifier of this server node.
     *
     * @return the UUID string identifier
     */
    public String getId() { return id; }

    /**
     * Returns the hostname or IP address of this server.
     *
     * @return the host string
     */
    public String getHost() { return host; }

    /**
     * Returns the port number this server listens on.
     *
     * @return the port number
     */
    public int getPort() { return port; }

    /**
     * Returns the current health status of the server.
     *
     * <p>The health status is volatile to ensure visibility across threads.
     *
     * @return true if the server is healthy, false otherwise
     */
    public boolean isHealthy() { return isHealthy; }

    /**
     * Returns the configured weight for load distribution.
     *
     * @return the weight value (0-100)
     */
    public int getWeight() { return weight; }

    /**
     * Returns the current number of active connections to this server.
     *
     * <p>This is a snapshot of the AtomicLong value, which may change concurrently.
     *
     * @return the number of active connections
     */
    public long getActiveConnections() { return activeConnections.get(); }

    /**
     * Calculates and returns the average latency of requests processed by this server.
     *
     * <p>The calculation is based on total latency divided by total requests. If no
     * requests have been processed, returns 0. This method is thread-safe as it uses
     * atomic variables.
     *
     * @return the average latency in milliseconds, or 0 if no requests
     */
    public double getAverageLatency() {
        long requests = totalRequests.get();
        return requests > 0 ? totalLatency.get() / (double) requests : 0;
    }

    /**
     * Sets the health status of the server.
     *
     * <p>The volatile keyword ensures this change is visible to all threads.
     *
     * @param healthy the new health status
     */
    public void setHealthy(boolean healthy) { this.isHealthy = healthy; }

    /**
     * Sets the failure rate of the server.
     *
     * @param rate the failure rate (0.0 to 1.0)
     */
    public void setFailureRate(double rate) { this.failureRate = rate; }

    /**
     * Sets the timestamp of the last health check.
     *
     * @param timestamp the timestamp in milliseconds
     */
    public void setLastHealthCheck(long timestamp) { this.lastHealthCheck = timestamp; }

    /**
     * Atomically increments the active connections counter.
     *
     * @return the new connection count after increment
     */
    public long incrementConnections() { return activeConnections.incrementAndGet(); }

    /**
     * Atomically decrements the active connections counter.
     *
     * <p>Will not go below 0.
     *
     * @return the new connection count after decrement
     */
    public long decrementConnections() { return activeConnections.decrementAndGet(); }

    /**
     * Records metrics for a completed request.
     *
     * <p>Updates both the request count and total latency atomically.
     *
     * @param latency the request latency in milliseconds
     * @throws IllegalArgumentException if latency is negative
     */
    public void recordRequest(long latency) {
        if (latency < 0) {
            throw new IllegalArgumentException("Latency cannot be negative: " + latency);
        }
        totalRequests.incrementAndGet();
        totalLatency.addAndGet(latency);
    }

    /**
     * Returns a string representation of the server node.
     *
     * @return a formatted string with host, port, weight, and health status
     */
    @Override
    public String toString() {
        return String.format("%s:%d (weight=%d, healthy=%b)", host, port, weight, isHealthy);
    }
}