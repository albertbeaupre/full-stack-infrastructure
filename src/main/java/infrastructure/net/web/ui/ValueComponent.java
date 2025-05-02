package infrastructure.net.web.ui;

import infrastructure.net.web.ui.event.ValueChangeEvent;

/**
 * Base class for components that hold and manage a textual value.
 * <p>
 * Extends {@link Component} to provide:
 * <ul>
 *   <li>Standardized lifecycle handling for value-based UI elements.</li>
 *   <li>Automatic wiring of input events to update the component’s value.</li>
 * </ul>
 * <p>
 * Subclasses must implement {@link #setValue(String)} and {@link #getValue()}
 * to define how values are rendered and retrieved in the UI. The
 * {@link #create()} hook is overridden to register a listener that applies
 * incoming value-change events to the component’s state.
 * <p>
 * Example usage:
 * <pre>
 * public class TextFieldComponent extends ValueComponent {
 *     private final InputElement input = ...;
 *
 *     public TextFieldComponent() {
 *         super("input");
 *         input.setAttribute("type", "text");
 *     }
 *
 *     {@literal @}Override
 *     public void setValue(String value) {
 *         input.setValue(value);
 *     }
 *
 *     {@literal @}Override
 *     public String getValue() {
 *         return input.getValue();
 *     }
 * }
 * </pre>
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public abstract class ValueComponent extends Component {

    /**
     * Constructs a new ValueComponent with the given HTML tag.
     * <p>
     * Delegates to {@link Component#Component(String)}, which initializes
     * the component’s tag, style scope, and registration in the UI context.
     *
     * @param tag the HTML tag name to render (e.g., "input", "textarea")
     */
    public ValueComponent(String tag) {
        super(tag);
    }

    /**
     * Updates the component’s displayed and internal value.
     * <p>
     * Invoked both:
     * <ul>
     *   <li>Programmatically by application code to set the initial or updated value.</li>
     *   <li>Automatically via a value-change event listener when the user edits the field.</li>
     * </ul>
     *
     * @param value the new value to render in the component
     */
    public abstract void setValue(String value);

    /**
     * Retrieves the current value held by the component.
     * <p>
     * Applications should call this method to read user input or
     * programmatic value changes.
     *
     * @return the component’s current value as a string
     */
    public abstract String getValue();

    /**
     * Lifecycle hook called when this component is attached to the DOM.
     * <p>
     * Registers a {@link ValueChangeEvent} listener so that whenever the
     * underlying DOM element fires an "input" event, the new value is
     * passed to {@link #setValue(String)} for synchronization.
     * <p>
     * Subclasses should not override without invoking super.create(), or
     * they may lose the automatic input wiring.
     */
    @Override
    protected void create() {
        this.addValueChangeListener((ValueChangeEvent e) -> setValue(e.getNewValue()));
    }
}