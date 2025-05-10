package infrastructure.net.web.ui;

import infrastructure.net.web.ui.event.ValueChangeEvent;

/**
 * Base class for UI components that hold a typed value and synchronize it
 * with an underlying DOM element.
 * <p>
 * This generic {@code ValueComponent<V>}:
 * <ul>
 *   <li>Maintains a value of type {@code V} in its server‐side state;</li>
 *   <li>Defines two abstract methods, {@link #deconstruct(String)} and {@link #construct(V)},
 *       to convert between the component’s textual DOM representation and its typed value;</li>
 *   <li>Automatically wires browser "input" events (fired as {@link ValueChangeEvent})
 *       to update the server‐side value via {@link #deconstruct(String)};</li>
 *   <li>Provides {@link #setValue(V)} and {@link #getValue()} for programmatic value access
 *       and synchronization back to the client via {@link DOMUpdateType#SET_VALUE}.</li>
 * </ul>
 * <p>
 * Subclasses must implement:
 * <ul>
 *   <li>{@link #deconstruct(String)} to parse the raw string from the DOM into {@code V};</li>
 *   <li>{@link #construct(V)} to serialize {@code V} into a string for the DOM.</li>
 * </ul>
 * <p>
 * During {@link #create()}, the component registers a listener that converts incoming
 * string values into {@code V} and calls {@link #setValue(V)} to keep both sides in sync.
 *
 * @param <V> the type of the value managed by this component
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public abstract class ValueComponent<V> extends Component {

    /**
     * Server‐side state of this component’s value.
     */
    private V value;

    /**
     * Constructs a new {@code ValueComponent} for the given HTML tag.
     * <p>
     * Delegates to {@link Component#Component(String)}, which initializes
     * tag, style, and registration in the UI context.
     *
     * @param tag the HTML element tag (e.g., "input", "textarea")
     */
    public ValueComponent(String tag) {
        super(tag);
    }

    /**
     * Converts the raw string value from the DOM into the typed {@code V}.
     * <p>
     * Invoked when an "input" event fires in the browser. Implementations
     * should handle parsing and validation, throwing unchecked exceptions
     * for invalid input if necessary.
     *
     * @param value the raw string from the DOM
     * @return the parsed value of type {@code V}
     */
    public abstract V deconstruct(String value);

    /**
     * Converts the typed value {@code V} into its string representation
     * for the DOM.
     * <p>
     * Called when {@link #setValue(V)} is invoked to synchronize the
     * server‐side value back to the client.
     *
     * @param value the typed value
     * @return the string to set in the DOM
     */
    public abstract String construct(V value);

    /**
     * Updates this component’s value both server‐side and in the client DOM.
     * <p>
     * Stores the new value, then queues a {@link DOMUpdateType#SET_VALUE}
     * operation with parameter {@link DOMUpdateParam#VALUE} set to
     * {@link #construct(V) construct(value)}, and flushes immediately
     * if attached.
     *
     * @param value the new value to apply
     */
    public void setValue(V value) {
        this.value = value;
        this.queueForDispatch(DOMUpdateType.SET_VALUE, DOMUpdateParam.VALUE, construct(value));
        this.push();
    }

    /**
     * Retrieves the current server‐side value of this component.
     *
     * @return the current value of type {@code V}, or {@code null} if none set
     */
    public V getValue() {
        return value;
    }

    /**
     * Lifecycle hook invoked when this component is attached to the DOM.
     * <p>
     * Registers a listener for {@link ValueChangeEvent}, which supplies
     * the raw new string value. That string is passed through
     * {@link #deconstruct(String)} and then {@link #setValue(V)} is called
     * to update both sides.
     * <p>
     * Subclasses overriding this method must invoke {@code super.create()}
     * to maintain the event wiring.
     */
    @Override
    protected void create() {
        this.addValueChangeListener(e -> setValue(deconstruct(e.getNewValue())));
    }

    @Override
    protected void destroy() { }
}