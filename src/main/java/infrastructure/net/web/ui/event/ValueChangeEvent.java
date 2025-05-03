package infrastructure.net.web.ui.event;

import infrastructure.event.Event;
import infrastructure.net.web.ui.Component;

/**
 * Fired when a {@link Component} that holds a value (e.g., a {@code ValueComponent})
 * has its value changed, either programmatically or via user interaction.
 * <p>
 * Carries the source component and both the previous and updated string values.
 * Useful for listeners that need to react to or validate value transitions.
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class ValueChangeEvent extends Event {

    /**
     * The component whose value has changed.
     */
    private final Component component;

    /**
     * The previous value before the change occurred.
     */
    private final String oldValue;

    /**
     * The new value after the change occurred.
     */
    private final String newValue;

    /**
     * Constructs a new ValueChangeEvent.
     *
     * @param component the source component that fired the event
     * @param oldValue  the value before the change (maybe {@code null} if none)
     * @param newValue  the value after the change (maybe {@code null} if cleared)
     */
    public ValueChangeEvent(Component component, String oldValue, String newValue) {
        this.component = component;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the component whose value changed.
     *
     * @return the source {@link Component}
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns the value before the change.
     *
     * @return the previous value, or {@code null} if there was none
     */
    public String getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value after the change.
     *
     * @return the new value, or {@code null} if the value was cleared
     */
    public String getNewValue() {
        return newValue;
    }
}