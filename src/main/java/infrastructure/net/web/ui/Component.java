package infrastructure.net.web.ui;

import infrastructure.event.Event;
import infrastructure.event.EventListener;
import infrastructure.event.EventPublisher;
import infrastructure.net.web.SessionContext;
import infrastructure.net.web.ui.css.Style;
import infrastructure.net.web.ui.css.StyleChangeEvent;
import infrastructure.net.web.ui.event.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a generic UI component in a hierarchical structure. This is an abstract class
 * that provides base functionality for creating and managing custom components.
 * Components are constructed with a specific HTML tag and can be used to build complex
 * UI structures by arranging child components within parent components.
 * <p>
 * Each component is associated with a unique identifier and a {@link Style} object
 * to allow dynamic styling updates. The class manages the lifecycle of components
 * by providing methods to add and remove child components and offers mechanisms for
 * event handling and DOM updates.
 * <p>
 * Components must be subclassed, and their lifecycle methods {@code create} and {@code destroy}
 * must be implemented to define specific behaviors during the component's initialization
 * and cleanup phases, respectively.
 * <p>
 * This class also facilitates event-based interactions, allowing components to listen
 * for and handle various events, such as style changes, value changes, and user input events.
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since May 2nd, 2025
 */
public abstract class Component implements EventListener<StyleChangeEvent> {

    /**
     * Collects and queues DOMUpdate operations for this component.
     */
    private final DOMDispatcher dispatcher = new DOMDispatcher();

    /**
     * Holds child components nested under this component.
     */
    private ArrayList<Component> children;

    /**
     * Responsible for managing the publishing and subscription of events within the component.
     */
    private EventPublisher publisher;

    /**
     * Manages CSS styles for this component, identified by its component ID.
     */
    private final Style style;

    /**
     * Reference to the top-level UI context this component belongs to.
     */
    private final UI ui;

    /**
     * The HTML tag name used to render this component (e.g., "div", "span", "button").
     */
    private final String tag;

    /**
     * Parent component in the hierarchy, or {@code null} if this is a root UI.
     */
    private Component parent;

    /**
     * Unique identifier assigned by the {@link UI} for DOM mapping.
     */
    private int componentID;
    /**
     * True if this component is attached to the live DOM; false otherwise.
     */
    private boolean attached;

    /**
     * Represents the enabled state of the component.
     */
    private boolean enabled;

    /**
     * Constructs a new Component with the given HTML tag.
     * <p>
     * If this instance <em>is</em> a {@link UI}, it serves as its own context;
     * otherwise it retrieves the current UI from the {@link SessionContext} and
     * gets a new component ID. Initializes a {@link Style} object scoped to
     * the generated component ID.
     *
     * @param tag the HTML tag name to render for this component
     */
    public Component(String tag) {
        this.tag = tag;
        if (this instanceof UI ui) {
            this.ui = ui;
        } else {
            this.ui = SessionContext.get().getUI();
            this.componentID = this.ui.nextComponentID();
        }
        this.style = new Style(this, "#" + this.getComponentID());
    }

    /**
     * Lifecycle hook invoked when the component is added to its parent.
     * <p>
     * Subclasses should override this method to perform initialization logic,
     * such as setting up child elements or default event listeners.
     */
    protected abstract void create();

    /**
     * Lifecycle hook invoked when the component is removed from its parent.
     * <p>
     * Subclasses should override this method to perform cleanup logic,
     * such as releasing resources or unsubscribing from services.
     */
    protected abstract void destroy();

    /**
     * Adds one or more child parts to this component.
     * <p>
     * Each child is registered with the UI context, queued for a DOM append update,
     * initialized via {@link #create()}, and marked as attached if this component
     * is currently attached. Finally, all queued updates (including nested children)
     * are flushed to the client.
     *
     * @param children one or more parts to append as children
     */
    public void add(Component... children) {
        if (this.children == null)
            this.children = new ArrayList<>();

        for (Component child : children) {
            this.ui.register(child.componentID, child);
            this.children.add(child);
            this.queueForDispatch(DOMUpdateType.APPEND_CHILD, Map.of(DOMUpdateParam.IDENTIFIER, child.componentID, DOMUpdateParam.HTML, child.tag));
            child.create();
            child.parent = this;
            child.attached = this.isAttached();
        }
        push();
    }

    /**
     * Removes one or more child parts from this component.
     * <p>
     * Each child is detached, queued for a DOM remove update, recycled in the UI,
     * and cleaned up via {@link #destroy()}. Finally, all queued updates
     * (including nested children) are flushed to the client.
     *
     * @param children one or more parts to remove
     */
    public void remove(Component... children) {
        if (this.children == null)
            return;

        for (Component child : children) {
            child.parent = null;
            child.attached = false;
            this.children.remove(child);
            this.dispatcher.queue(new DOMUpdate(DOMUpdateType.REMOVE, child.componentID));
            this.ui.recycle(child.componentID);
            child.destroy();
        }
        push();
    }

    /**
     * Handles a {@link StyleChangeEvent} by queuing a corresponding DOM style update.
     * <p>
     * Called automatically when the {@link Style} object fires change events.
     *
     * @param event the style change event containing the property and new value
     */
    @Override
    public void handle(StyleChangeEvent event) {
        this.dispatcher.queue(new DOMUpdate(DOMUpdateType.SET_STYLE, componentID).param(DOMUpdateParam.STYLE_PROPERTY, event.getProperty()).param(DOMUpdateParam.STYLE_VALUE, event.getValue()));
        push();
    }

    /**
     * Registers an event listener for a specific event type and binds it to a DOM event.
     * <p>
     * If not already registered, queues a DOM update to add the corresponding browser
     * event listener. Then registers the listener with the internal {@link EventPublisher}.
     *
     * @param <T>      the event subtype
     * @param clazz    the class object of the event type
     * @param listener the listener to notify when the event occurs
     * @param name     the DOM event name (e.g., "click", "keydown")
     */
    private <T extends Event> void registerEventListener(Class<T> clazz, EventListener<T> listener, String name) {
        if (this.publisher == null)
            this.publisher = new EventPublisher();

        if (!this.publisher.isRegistered(clazz)) {
            this.dispatcher.queue(new DOMUpdate(DOMUpdateType.ADD_EVENT_LISTENER, componentID).param(DOMUpdateParam.EVENT_NAME, name));
        }
        this.publisher.register(clazz, listener);
        push();
    }


    /**
     * Publishes the specified event to all registered listeners for processing.
     */
    public void publish(Event event) {
        this.publisher.publish(event);
    }

    /**
     * Attaches a listener for value-change events (e.g., input fields).
     *
     * @param listener the listener to invoke on value changes
     */
    public void addValueChangeListener(EventListener<ValueChangeEvent> listener) {
        registerEventListener(ValueChangeEvent.class, listener, "input");
    }

    /**
     * Attaches a listener for key-up events.
     *
     * @param listener the listener to invoke on key-up actions
     */
    public void addKeyUpListener(EventListener<KeyUpEvent> listener) {
        registerEventListener(KeyUpEvent.class, listener, "keyup");
    }

    /**
     * Attaches a listener for key-down events.
     *
     * @param listener the listener to invoke on key-down actions
     */
    public void addKeyDownListener(EventListener<KeyDownEvent> listener) {
        registerEventListener(KeyDownEvent.class, listener, "keydown");
    }

    /**
     * Attaches a listener for click events.
     *
     * @param listener the listener to invoke on click actions
     */
    public void addClickListener(EventListener<ClickEvent> listener) {
        registerEventListener(ClickEvent.class, listener, "click");
    }

    /**
     * Flushes all queued {@link DOMUpdate DOMUpdates} to the client channel
     * if this component is attached. Recursively invokes {@code push()}
     * on all child components to ensure nested updates are sent.
     */
    protected void push() {
        if (isAttached()) {
            this.dispatcher.flush(SessionContext.get().getChannel());
            if (this.children != null) {
                for (Component child : children)
                    child.push();
            }
        }
    }

    /**
     * Removes all child components from this component and queues a DOM update
     * operation to clear the corresponding child elements in the UI.
     * <p>
     * This method clears the internal children list and invokes the
     * {@link #queueForDispatch(DOMUpdateType)} method with the
     * {@link DOMUpdateType#CLEAR_CHILDREN} type to ensure the client-side DOM
     * reflects the removal.
     */
    public void clear() {
        if (this.children != null)
            this.children.clear();
        this.queueForDispatch(DOMUpdateType.CLEAR_CHILDREN);
        push();
    }

    /**
     * Determines whether this component is currently attached to the DOM.
     * <p>
     * A component is considered attached if its own {@code attached} flag is true,
     * or if any ancestor component is attached.
     *
     * @return {@code true} if attached; {@code false} otherwise
     */
    public boolean isAttached() {
        return attached || (parent != null && parent.isAttached());
    }

    /**
     * Returns the {@link Style} object associated with this component.
     *
     * @return the style instance for dynamic CSS updates
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Queues a DOM update operation for dispatch with the specified update type.
     * Constructs a {@link DOMUpdate} instance using the provided update type and
     * the component's unique identifier, then queues it for processing in the dispatcher.
     *
     * @param type the type of DOM update to queue (e.g., updates related to attributes, styles, or child elements)
     */
    public void queueForDispatch(DOMUpdateType type) {
        this.dispatcher.queue(new DOMUpdate(type, getComponentID()));
    }

    /**
     * Queues a DOM update operation for dispatch with the specified update type and parameters.
     * Constructs a {@link DOMUpdate} instance using the provided update type, component ID,
     * and parameters, then queues it for processing in the dispatcher.
     *
     * @param type       the type of DOM update to queue (e.g., updates related to attributes, styles, or child elements)
     * @param parameters a map of parameters specifying the details of the DOM update (e.g., attributes or values)
     */
    public void queueForDispatch(DOMUpdateType type, Map<DOMUpdateParam, Object> parameters) {
        this.dispatcher.queue(new DOMUpdate(type, getComponentID()).params(parameters));
    }

    /**
     * Queues a DOM update operation for dispatch with specific type, parameter, and value.
     * This method constructs a {@link DOMUpdate} instance using the provided update type,
     * component ID, parameter, and value, and queues it for processing in the dispatcher.
     *
     * @param type  the type of DOM update to queue (e.g., setting an attribute or appending a child)
     * @param param the parameter defining the aspect of the DOM to be updated (e.g., an attribute or property)
     * @param value the value associated with the parameter for the update (e.g., the new attribute value)
     */
    public void queueForDispatch(DOMUpdateType type, DOMUpdateParam param, Object value) {
        this.dispatcher.queue(new DOMUpdate(type, getComponentID()).param(param, value));
    }

    /**
     * Returns the unique identifier assigned to this component by its UI.
     *
     * @return the component ID
     */
    public int getComponentID() {
        return componentID;
    }

    /**
     * Retrieves the parent component of this component.
     *
     * @return the parent component if it exists, or null if this component does not have a parent
     */
    public Component getParent() {
        return parent;
    }

    /**
     * Sets the enabled state of the component.
     * Queues a DOM update to toggle the "disabled" property based on the provided state.
     *
     * @param enabled {@code true} to enable the component, {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.queueForDispatch(DOMUpdateType.SET_PROPERTY, Map.of(DOMUpdateParam.PROPERTY, "disabled", DOMUpdateParam.VALUE, !enabled));
        this.push();
    }

    /**
     * Determines whether this component is currently enabled.
     * The enabled state affects the component's interactivity and may
     * control whether it is usable or interactive within the UI.
     *
     * @return {@code true} if the component is enabled; {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
}