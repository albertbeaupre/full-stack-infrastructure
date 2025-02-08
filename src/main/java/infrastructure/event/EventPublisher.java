package infrastructure.event;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeSet;

/**
 * The {@code EventPublisher} class is responsible for managing the publication of
 * events and the registration of listeners that handle these events. This class
 * acts as a central hub for event-driven architecture, allowing different parts
 * of an application to communicate with each other through events.
 *
 * <p>For an {@code Event} to be processed, an {@code EventListener} must first be
 * registered with this {@code EventPublisher}. When an event is published, all
 * registered listeners for that specific event type are notified and given the
 * opportunity to handle the event. The order in which listeners are notified is
 * determined by their priority, which can be specified using the {@link EventPriority}
 * annotation.
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * EventPublisher eventPublisher = new EventPublisher();
 *
 * eventPublisher.register(MyEvent.class, new MyEventListener());
 *
 * MyEvent event = new MyEvent();
 * eventPublisher.publish(event);
 * }</pre>
 *
 * <p>In the above example, a listener for {@code MyEvent} is registered with the
 * {@code EventPublisher}, and an instance of {@code MyEvent} is published. The
 * listener will handle the event according to its defined behavior.
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>The {@code EventPublisher} class is thread-safe, as both the registration
 * and publication methods are synchronized. This ensures that listeners can be
 * safely registered and events can be safely published from multiple threads.
 *
 * @author Albert Beaupre
 * @see Event
 * @see EventListener
 * @see EventPriority
 * @see TreeSet
 * @since August 29th, 2024
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EventPublisher {

    /**
     * A mapping of event types to sets of event listeners. The keys are classes
     * that extend {@link Event}, and the values are {@link TreeSet} collections
     * of {@link EventListener} instances. The listeners are stored in a
     * {@code TreeSet} to maintain order based on their priority.
     */
    private final HashMap<Class<? extends Event>, TreeSet<EventListener>> listeners = new HashMap<>();

    /**
     * Registers the given {@code EventListener} to listen for events of the specified
     * type. The listener is added to a set of listeners associated with the event class.
     *
     * <p>The {@code TreeSet} ensures that listeners are ordered by their priority,
     * as defined by the {@link EventPriority} annotation. If no priority is specified,
     * a default priority of 0 is used.
     *
     * <p>This method is synchronized to ensure thread safety during the registration
     * process.
     *
     * @param clazz    the class of the event type that the listener is interested in.
     * @param listener the listener that should be notified when the event is published.
     * @throws RuntimeException if the listener's {@code handle} method cannot be found.
     */
    public synchronized void register(Class<? extends Event> clazz, EventListener listener) {
        TreeSet<EventListener> set = this.listeners.getOrDefault(clazz, createSet(clazz));
        set.add(listener);
        this.listeners.put(clazz, set);
    }

    /**
     * Publishes the given {@code Event} to all registered listeners. Each listener
     * will handle the event in the order determined by its priority. If the event is
     * consumed (i.e., no further processing is needed), the publishing process is
     * stopped.
     *
     * <p>The listeners are retrieved based on the event's class, and each listener's
     * {@code handle} method is invoked to process the event. If the event has been
     * marked as consumed by any listener, subsequent listeners will not be notified.
     *
     * <p>This method is synchronized to ensure that events are published safely
     * across multiple threads.
     *
     * @param event the event to publish.
     */
    public synchronized void publish(Event event) {
        TreeSet<EventListener> set = this.listeners.get(event.getClass());
        if (Objects.nonNull(set)) {
            for (EventListener listener : set) {
                if (event.consumed()) {
                    break;
                }
                listener.handle(event);
            }
        }
    }

    /**
     * Creates a {@code TreeSet} for storing {@code EventListener} instances, using
     * a comparator that orders listeners by their priority. The comparator retrieves
     * the priority from the {@link EventPriority} annotation on the listener's
     * {@code handle} method. Listeners with higher priority values are ordered before
     * those with lower priority values.
     *
     * <p>If the {@code handle} method of a listener does not have an {@code EventPriority}
     * annotation, a default priority of 0 is used.
     *
     * @param clazz the class of the event type that the listeners will handle.
     * @return a {@code TreeSet} of {@code EventListener} instances ordered by priority.
     * @throws RuntimeException if the listener's {@code handle} method cannot be found.
     */
    private TreeSet<EventListener> createSet(Class<? extends Event> clazz) {
        return new TreeSet<>((listener1, listener2) -> {
            try {
                Method method1 = listener1.getClass().getMethod("handle", clazz);
                Method method2 = listener2.getClass().getMethod("handle", clazz);
                EventPriority annotation1 = method1.getAnnotation(EventPriority.class);
                EventPriority annotation2 = method2.getAnnotation(EventPriority.class);
                int priority1 = Objects.nonNull(annotation1) ? annotation1.priority() : 0;
                int priority2 = Objects.nonNull(annotation2) ? annotation2.priority() : 0;

                return Integer.compare(priority2, priority1);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }
}