package infrastructure.net.web.ui.event;

import infrastructure.event.Event;

/**
 * Represents a keyboard event sent from the client-side.
 * Models a key press or release including modifier states and optional metadata.
 *
 * <p>The {@code key} field maps to a predefined enum of keyboard keys,
 * while {@code down} reflects whether the event is a press ({@code true}) or release ({@code false}).
 *
 * <p>Modifier keys such as Ctrl, Alt, Shift, and Meta are encoded using bit flags
 * in {@code modifierFlags}, and {@code repeated} indicates whether the key was
 * auto-repeated due to being held.
 *
 * @author Albert
 * @version 1.0
 * @since April 19, 2025
 */
public class KeyUpEvent extends Event {

    /**
     * Bit flag indicating the Control key was pressed.
     */
    private static final int CTRL = 1;

    /**
     * Bit flag indicating the Shift key was pressed.
     */
    private static final int SHIFT = 1 << 1;

    /**
     * Bit flag indicating the Alt key was pressed.
     */
    private static final int ALT = 1 << 2;

    /**
     * Bit flag indicating the Meta key was pressed (e.g., Windows or Command key).
     */
    private static final int META = 1 << 3;

    private final Key key;
    private final boolean repeated;
    private final int modifierFlags;

    /**
     * Constructs a {@code KeyEvent} representing a key press or release.
     *
     * @param key           the logical key involved in the event
     * @param repeated      {@code true} if the event is an auto-repeat
     * @param modifierFlags bitwise OR of modifier keys (CTRL, SHIFT, etc.)
     */
    public KeyUpEvent(Key key, boolean repeated, int modifierFlags) {
        this.key = key;
        this.repeated = repeated;
        this.modifierFlags = modifierFlags;
    }

    /**
     * @return the logical key involved in this event
     */
    public Key getKey() {
        return key;
    }

    /**
     * @return {@code true} if this is an auto-repeat event
     */
    public boolean isRepeated() {
        return repeated;
    }

    /**
     * @return the bitmask representing the state of modifier keys
     */
    public int getModifierFlags() {
        return modifierFlags;
    }

    /**
     * @return {@code true} if the Ctrl key was held during the event
     */
    public boolean isCtrlPressed() {
        return (modifierFlags & CTRL) != 0;
    }

    /**
     * @return {@code true} if the Shift key was held during the event
     */
    public boolean isShiftPressed() {
        return (modifierFlags & SHIFT) != 0;
    }

    /**
     * @return {@code true} if the Alt key was held during the event
     */
    public boolean isAltPressed() {
        return (modifierFlags & ALT) != 0;
    }

    /**
     * @return {@code true} if the Meta key was held during the event
     */
    public boolean isMetaPressed() {
        return (modifierFlags & META) != 0;
    }
}