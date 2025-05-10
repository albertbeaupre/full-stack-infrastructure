package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;

import java.util.Map;

/**
 * A concrete {@link Component} that renders a first-level heading (&lt;h1&gt;) element.
 * <p>
 * This component:
 * <ul>
 *   <li>Is represented by an {@code <h1>} HTML tag in the DOM;</li>
 *   <li>Allows programmatic updates of its text content via {@link #setText(String)};</li>
 *   <li>Automatically dispatches DOM updates to sync text changes to the client.</li>
 * </ul>
 * <p>
 * Subclasses may override lifecycle hooks to perform additional setup or cleanup,
 * but basic heading functionality is provided here.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class H1 extends Component {

    /**
     * The current text content of this heading.
     */
    private String text;

    /**
     * Constructs a new H1 heading with the given initial text.
     * <p>
     * Initializes this component as an {@code <h1>} element and sets
     * its text content, dispatching the corresponding DOM update.
     *
     * @param text the initial heading text to display
     */
    public H1(String text) {
        super("h1");
        setText(text);
    }

    /**
     * Updates the heading’s displayed text.
     * <p>
     * Stores the new text, queues a {@link DOMUpdateType#SET_TEXT} operation
     * with parameter {@link DOMUpdateParam#TEXT}, and immediately flushes
     * the update if the component is attached.
     *
     * @param text the new text content for this heading
     */
    public void setText(String text) {
        this.text = text;
        this.queueForDispatch(
                DOMUpdateType.SET_TEXT,
                Map.of(DOMUpdateParam.TEXT, text)
        );
        this.push();
    }

    /**
     * Returns the current text content of this heading.
     *
     * @return the text content of the heading
     */
    public String getText() {
        return text;
    }

    /**
     * Lifecycle hook invoked when this component is added to the DOM.
     * <p>
     * Default implementation does nothing; override to perform setup logic
     * such as registering event listeners or applying initial styles.
     */
    @Override
    protected void create() {
        // No additional setup required for basic H1
    }

    /**
     * Lifecycle hook invoked when this component is removed from the DOM.
     * <p>
     * Default implementation does nothing; override to perform cleanup logic
     * such as unregistering listeners or releasing resources.
     */
    @Override
    protected void destroy() {
        // No resources to clean up for H1
    }
}