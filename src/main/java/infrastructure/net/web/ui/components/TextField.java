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
 * </ul>
 * <p>
 * Subclasses may override lifecycle methods to add additional behavior,
 * but the basic text‐input functionality is provided here.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class TextField extends ValueComponent {

    /**
     * Holds the current text content of this field.
     */
    private String value;

    /**
     * Constructs a new TextField component.
     * <p>
     * Initializes this component as an {@code <input>} element whose
     * {@code type} attribute will be set to "text" during creation.
     */
    public TextField() {
        super("input");
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
        this.queueForDispatch(
                DOMUpdateType.SET_TYPE,
                Map.of(DOMUpdateParam.TYPE, "text")
        );
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

    /**
     * Updates the internal text value of this component.
     * <p>
     * This method is called both by application code and automatically
     * in response to user input events.
     *
     * @param value the new text to store in this field
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Retrieves the current text stored in this field.
     *
     * @return the field's current text value
     */
    @Override
    public String getValue() {
        return value;
    }
}