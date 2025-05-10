package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;
import infrastructure.net.web.ui.ValueComponent;

import java.util.Map;

/**
 * A concrete {@link ValueComponent} that renders a single-line text input field.
 * <p>
 * This component:
 * <ul>
 *   <li>Is represented by an {@code <input type="text">} element.</li>
 *   <li>Automatically wires its "input" events to update its internal value.</li>
 *   <li>Provides methods to programmatically set and retrieve the text.</li>
 *   <li>Supports an optional placeholder attribute displayed when the field is empty.</li>
 * </ul>
 * <p>
 * Subclasses may override lifecycle methods to add additional behavior,
 * but the basic text-input functionality is provided here.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class TextField extends ValueComponent<String> {

    /**
     * Placeholder text displayed when the input is empty.
     */
    private String placeholder;

    /**
     * Constructs a new TextField component with no placeholder.
     * <p>
     * Initializes this component as an {@code <input>} element whose
     * {@code type} attribute will be set to "text" during creation.
     */
    public TextField() {
        super("input");
    }

    /**
     * Constructs a new TextField component with the given placeholder text.
     * <p>
     * The placeholder will be applied immediately and displayed in the
     * input element until the user enters a value.
     *
     * @param placeholder the placeholder text to display when the field is empty
     */
    public TextField(String placeholder) {
        super("input");
        setPlaceholder(placeholder);
    }

    /**
     * Constructs a new TextField component with the given placeholder text and initial value.
     * <p>
     * The placeholder will be applied immediately and displayed in the input
     * element until the user enters a value. The initial value will also be
     * set during component initialization.
     *
     * @param placeholder the placeholder text to display when the field is empty
     * @param value       the initial value to populate the TextField
     */
    public TextField(String placeholder, String value) {
        this(placeholder);
        setValue(value);
    }

    /**
     * Converts the raw string from the DOM into the component’s typed value.
     * <p>
     * For a text field, this is a no-op: the raw string is returned directly.
     *
     * @param value the raw input string from the DOM
     * @return the parsed value of type {@code String}
     */
    @Override
    public String deconstruct(String value) {
        return value;
    }

    /**
     * Converts the component’s typed value into a string suitable for the DOM.
     * <p>
     * For a text field, this is a no-op: the value string is returned directly.
     *
     * @param value the typed value to render
     * @return the string to set in the DOM input element
     */
    @Override
    public String construct(String value) {
        return value;
    }

    /**
     * Updates the placeholder attribute of the underlying input element.
     * <p>
     * Queues a {@link DOMUpdateType#SET_ATTRIBUTE} update to set the
     * {@code placeholder} attribute to the specified text and flushes
     * the update immediately if the component is attached.
     *
     * @param text the new placeholder text to display (null or empty clears it)
     */
    public void setPlaceholder(String text) {
        this.placeholder = text;
        this.queueForDispatch(DOMUpdateType.SET_ATTRIBUTE, Map.of(DOMUpdateParam.KEY, "placeholder", DOMUpdateParam.VALUE, text));
        this.push();
    }

    /**
     * Returns the currently configured placeholder text.
     *
     * @return the placeholder text, or {@code null} if none has been set
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Lifecycle hook invoked when this component is attached to the DOM.
     * <p>
     * Calls {@code super.create()} to register the "input" event listener
     * (for value synchronization), then enqueues a DOM update to set
     * the underlying element's {@code type} attribute to "text".
     */
    @Override
    protected void create() {
        super.create();
        this.queueForDispatch(DOMUpdateType.SET_TYPE, Map.of(DOMUpdateParam.TYPE, "text"));
    }

    /**
     * Lifecycle hook invoked when this component is removed from the DOM.
     * <p>
     * Provides a place for cleanup logic such as unregistering listeners
     * or freeing resources. No cleanup is required for a basic text field.
     */
    @Override
    protected void destroy() {
        // No resources to clean up for TextField
    }
}