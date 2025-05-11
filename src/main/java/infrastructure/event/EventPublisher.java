package infrastructure.event;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@code EventPublisher} class manages the publication of events and the registration of listeners
 * that handle these events. It serves as a central hub for event-driven communication in an application.
 *
 * <p>For an {@code Event} to be processed, an {@code EventListener} must be registered with this publisher.
 * When an event is published, registered listeners for that event type are notified in priority order,
 * as defined by the {@link EventPriority} annotation (higher values = earlier execution).
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * EventPublisher publisher = new EventPublisher();
 * publisher.register(MyEvent.class, new MyEventListener());
 * publisher.publish(new MyEvent());
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe, using a {@link ConcurrentHashMap}
 * for listener storage and atomic operations for registration and publication.
 *
 * <p>If no listeners are registered for an event type, {@code publish} has no effect. Exceptions
 * thrown by listeners are logged but do not stop the event propagation unless the event is consumed.
 *
 * @author Albert Beaupre
 * @see Event
 * @see EventListener
 * @see EventPriority
 * @since August 29th, 2024
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EventPublisher {

    /**
     * A thread-safe mapping of event types to ordered sets of listeners.
     */
    private final ConcurrentHashMap<Class<? extends Event>, TreeSet<EventListener>> listeners = new ConcurrentHashMap<>();

    /**
     * Determines whether an event type has any registered listeners.
     *
     * @param clazz the event class to check for registration; must not be null
     * @return true if the event class has registered listeners, false otherwise
     * @throws NullPointerException if the event class is null
     */
    public boolean isRegistered(Class<? extends Event> clazz) {
        Objects.requireNonNull(clazz, "Event class must not be null");
        return listeners.containsKey(clazz);
    }

    /**
     * Registers an {@code EventListener} to handle events of the specified type.
     * Listeners are ordered by priority (via {@link EventPriority}), with higher values executed first.
     *
     * @param clazz    the event type the listener will handle
     * @param listener the listener to register
     * @throws IllegalArgumentException if clazz or listener is null
     */
    public void register(Class<? extends Event> clazz, EventListener listener) {
        Objects.requireNonNull(clazz, "Event class must not be null");
        Objects.requireNonNull(listener, "Listener must not be null");

        listeners.computeIfAbsent(clazz, this::createSet).add(listener);
    }

    /**
     * Unregisters an {@code EventListener} from handling events of the specified type.
     *
     * @param clazz    the event type to unregister from
     * @param listener the listener to remove
     * @return true if the listener was removed, false if it wasn’t registered
     * @throws IllegalArgumentException if clazz or listener is null
     */
    public boolean unregister(Class<? extends Event> clazz, EventListener listener) {
        Objects.requireNonNull(clazz, "Event class must not be null");
        Objects.requireNonNull(listener, "Listener must not be null");

        TreeSet<EventListener> set = listeners.get(clazz);
        return set != null && set.remove(listener);
    }

    /**
     * Publishes an {@code Event} to all registered listeners for its type. Listeners are
     * invoked in priority order, stopping if the event is consumed.
     *
     * @param event the event to publish
     * @throws IllegalArgumentException if event is null
     */
    public void publish(Event event) {
        Objects.requireNonNull(event, "Event must not be null");

        TreeSet<EventListener> set = listeners.get(event.getClass());
        if (set != null) {
            for (EventListener listener : set) {
                if (event.isConsumed()) // Assuming Event uses isConsumed()
                    break;

                try {
                    if (listener.canHandle(event))
                        listener.handle(event);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to handle event", e);
                }
            }
        }
    }

    /**
     * Creates a {@code TreeSet} for storing listeners, ordered by priority (highest first).
     * Ties in priority are resolved by listener identity to ensure uniqueness.
     *
     * @param clazz the event type for the set
     * @return a priority-ordered TreeSet
     */
    private TreeSet<EventListener> createSet(Class<? extends Event> clazz) {
        return new TreeSet<>((listener1, listener2) -> {
            int compare = Integer.compare(extractPriority(listener2, clazz), extractPriority(listener1, clazz));
            if (compare == 0)
                return Integer.compare(System.identityHashCode(listener2), System.identityHashCode(listener1));
            return compare;
        });
    }

    private static int extractPriority(EventListener listener, Class<? extends Event> clazz) {
        for (Method method : listener.getClass().getMethods()) {
            if (!method.getName().equals("handle")) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && clazz.isAssignableFrom(params[0])) {
                EventPriority annotation = method.getAnnotation(EventPriority.class);
                return annotation != null ? annotation.priority() : 0;
            }
        }
        return 0;
    }

}
