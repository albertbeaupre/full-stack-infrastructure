package infrastructure.net.web.ui;

import infrastructure.collections.queue.IntUUIDQueue;
import infrastructure.event.Event;
import infrastructure.event.EventPublisher;
import infrastructure.net.web.Router;
import infrastructure.net.web.SessionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Root container for all UI components within a session.
 * <p>
 * The {@code UI} class:
 * <ul>
 *   <li>Serves as the root {@link Component} in the DOM hierarchy, represented by a {@code <div>} element;</li>
 *   <li>Allocates unique component IDs via an {@link IntUUIDQueue};</li>
 *   <li>Registers and recycles {@link Component} instances for efficient reuse;</li>
 *   <li>Provides a {@link Router} for handling navigation and mapping URIs to UI actions;</li>
 *   <li>Overrides {@link #isAttached()} to always return {@code true}, ensuring that
 *       updates on the root and its descendants are always flushed immediately.</li>
 * </ul>
 * <p>
 * Components should be registered with {@link #register(int, Component)} when added,
 * and reclaimed via {@link #recycle(int)} when removed to free up their IDs.
 * Use {@link #get(int)} to look up an existing component by its ID.
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since May 2, 2025
 */
public class UI extends Component {

    /**
     * Queue for generating and recycling integer-based component IDs.
     */
    private final IntUUIDQueue componentIDs = new IntUUIDQueue();

    /**
     * Map of active component instances keyed by their integer ID.
     */
    private final Map<Integer, Component> components = new HashMap<>();

    /**
     * An executor specifically designed for managing tasks associated with the UI.
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Publishes application-level events to registered listeners.
     */
    private final EventPublisher publisher = new EventPublisher();

    /**
     * Represents the router associated with the UI.
     */
    private final Router router = new Router(this);

    /**
     * Constructs the root UI container.
     * <p>
     * Initializes this component as a {@code <div>} element, then reserves
     * the first generated ID for the root itself by popping the
     * {@code componentIDs} queue so that subsequent IDs are used only for children.
     */
    public UI() {
        super("div");

        this.componentIDs.pop(); // Reserve the root component's ID so children start from the next ID
    }

    /**
     * Executes the given command safely within the UI context.
     * This ensures thread safety for component updates, similar to Vaadin's UI.access().
     *
     * @param command the runnable to execute in UI context
     */
    public void access(Runnable command) {
        final SessionContext current = SessionContext.get();
        executor.execute(() -> {
            SessionContext.set(current);
            try {
                command.run();
            } finally {
                SessionContext.clear();
            }
        });
    }

    /**
     * Publishes a custom application event to all registered listeners.
     *
     * @param event the event instance to dispatch
     */
    public void publish(Event event) {
        this.publisher.publish(event);
    }

    /**
     * Retrieves a registered component by its ID.
     *
     * @param componentID the unique ID of the component to look up
     * @return the corresponding {@link Component}, or {@code null} if not found
     */
    public Component get(int componentID) {
        return components.get(componentID);
    }

    /**
     * Allocates the next available integer component ID.
     *
     * @return a unique component ID for a new child component
     */
    public int nextComponentID() {
        return componentIDs.pop();
    }

    /**
     * Registers a component instance under its ID for future lookups and updates.
     *
     * @param componentID the unique ID of the component
     * @param component   the {@link Component} instance to register
     */
    public void register(int componentID, Component component) {
        this.components.put(componentID, component);
    }

    /**
     * Removes a component registration and recycles its ID back into the pool.
     *
     * @param componentID the ID of the component to remove
     * @return the removed {@link Component} instance, or {@code null} if none
     */
    public Component recycle(int componentID) {
        Component removed = this.components.remove(componentID);
        this.componentIDs.push(componentID);
        return removed;
    }

    /**
     * Retrieves the {@link EventPublisher} instance associated with the UI.
     * The {@code EventPublisher} is responsible for managing the publication
     * and handling of events within the application.
     *
     * @return the {@link EventPublisher} used for event-driven communication in this UI
     */
    protected EventPublisher getPublisher() {
        return publisher;
    }

    /**
     * No initialization required for the root UI container.
     * <p>
     * Subclasses may override to perform application-level setup,
     * but should call {@code super.create()} if they do.
     */
    @Override
    protected void create() {
    }

    /**
     * No cleanup required for the root UI container.
     * <p>
     * Subclasses may override to free resources when the UI is disposed of.
     */
    @Override
    protected void destroy() {
    }

    /**
     * Always returns {@code true} to indicate that the root UI is permanently
     * attached to the DOM, ensuring that {@link #push()} can flush updates.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAttached() {
        return true;
    }

    /**
     * Retrieves the {@link Router} instance associated with this {@code UI}.
     * The {@code Router} is responsible for managing navigation and route handling
     * within the application's user interface.
     *
     * @return the {@link Router} instance used for registering and handling routes
     */
    public Router getRouter() {
        return router;
    }
}