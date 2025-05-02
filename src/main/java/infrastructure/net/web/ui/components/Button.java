package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;
import infrastructure.net.web.ui.css.TextAlign;

import java.util.Map;

/**
 * A concrete {@link Component} that renders a clickable HTML <button> element.
 * <p>
 * This component:
 * <ul>
 *   <li>Is represented by a {@code <button>} tag in the DOM.</li>
 *   <li>Centers its text content by default using CSS text-align.</li>
 *   <li>Supports programmatic updates to its label text, automatically
 *       synchronizing with the client via a {@link DOMUpdateType#SET_TEXT} update.</li>
 * </ul>
 * <p>
 * Subclasses may override lifecycle hooks, but basic button behavior
 * (centering text and dispatching text changes) is provided here.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class Button extends Component {

    /**
     * The current label text displayed inside the button.
     */
    private String text;

    /**
     * Constructs a Button with no initial text.
     * <p>
     * Equivalent to {@link #Button(String) Button(null)}.
     */
    public Button() {
        this(null);
    }

    /**
     * Constructs a Button with the specified initial text.
     * <p>
     * If the provided {@code text} is non-null, it will be applied during
     * {@link #create()}, otherwise {@link #setText(String)} will be a no-op
     * until called explicitly.
     *
     * @param text the initial label text for this button, or {@code null}
     */
    public Button(String text) {
        super("button");
        this.text = text;
    }

    /**
     * Updates the button’s label text.
     * <p>
     * If {@code text} is non-null, this method:
     * <ul>
     *   <li>Stores the new text in the {@link #text} field.</li>
     *   <li>Queues a {@link DOMUpdateType#SET_TEXT} operation with the
     *       {@link DOMUpdateParam#TEXT} parameter for client synchronization.</li>
     * </ul>
     * If {@code text} is {@code null}, the call is ignored.
     *
     * @param text the new label text to display in the button
     */
    public void setText(String text) {
        if (text == null)
            return;

        this.text = text;
        this.queueForDispatch(DOMUpdateType.SET_TEXT, Map.of(DOMUpdateParam.TEXT, text));
        this.push();
    }

    /**
     * Lifecycle hook invoked when this component is added to the DOM.
     * <p>
     * Applies default styling (centered text) via the {@link #getStyle()}
     * API and sets the initial text by delegating to {@link #setText(String)}.
     * Subclasses should invoke {@code super.create()} to preserve this behavior.
     */
    @Override
    protected void create() {
        // Center the button’s label text
        this.getStyle().textAlign(TextAlign.CENTER);
        // Apply initial text if provided
        setText(text);
    }

    /**
     * Lifecycle hook invoked when this component is removed from the DOM.
     * <p>
     * Provides a place for cleanup logic (e.g., unregistering listeners
     * or freeing resources). No cleanup is required for a basic button.
     * Subclasses may override if needed.
     */
    @Override
    protected void destroy() {
        // No resources to clean up for Button
    }
}