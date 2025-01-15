package backend.event;

/**
 * The {@code EventListener} is used to listen for an event based on the
 * generic type assigned to the class. When an {@code Event} of that specific
 * type has been published to the {@code EventPublisher}, this listener will
 * handle that event.
 *
 * <p>
 * An {@code EventListener} must be registered to an {@code EventPublisher}
 * before it can listen for events.
 *
 * <p>
 * An {@code EventListener} can have priorities. Priorities are assigned by the
 * {@code @EventPriority} annotation and take an integer as the value for it. Higher
 * priority values cause the listener to be handled first.
 *
 * @author Albert Beaupre
 * @since August 27th, 2024
 * <E> The type of Event that this EventListener will listen for and handle.
 */
public interface EventListener<E extends Event> {

    /**
     * Handles the given {@code event} when an {@code EventPublisher} publishes it.
     *
     * <p>
     * The event being handled is of the same type assigned by the generic value of the class.
     *
     * <p>
     * This {@code EventListener} must be registered with the {@code EventPublisher} before
     * it can listen for that specific event.
     */
    void handle(E event);

}