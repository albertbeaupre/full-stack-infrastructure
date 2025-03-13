package infrastructure.event;

import java.time.Instant;

/**
 * An {@code Event} represents a publishable occurrence that can be handled by an {@code EventListener}
 * via an {@code EventPublisher}.
 *
 * <p>
 * Events can be consumed to prevent further processing by subsequent {@code EventListener}s.
 * Subclasses should define specific event types for domain-specific use cases.
 *
 * @author Albert Beaupre
 * @since August 29th, 2024
 */
public abstract class Event {

    private boolean consumed; // Flag indicating if the event has been consumed
    private final Instant timestamp; // Time the event was created

    /**
     * Constructs a new {@code Event} with the current timestamp.
     */
    protected Event() {
        this.timestamp = Instant.now();
    }

    /**
     * Returns {@code true} if this event has been consumed, preventing further handling;
     * {@code false} otherwise.
     *
     * @return whether the event is consumed
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Marks this event as consumed, preventing further handling by any subsequent
     * {@code EventListener}s. Throws an exception if already consumed.
     *
     * @throws IllegalStateException if the event has already been consumed
     */
    public void consume() {
        if (consumed) {
            throw new IllegalStateException("Event of type " + getClass().getSimpleName() + " was already consumed at " + timestamp);
        }
        this.consumed = true;
    }

    /**
     * Returns the timestamp when this event was created.
     *
     * @return the creation timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[consumed=" + consumed + ", timestamp=" + timestamp + "]";
    }
}