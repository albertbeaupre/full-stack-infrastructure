package infrastructure.web.ui.event;

import infrastructure.event.Event;

/**
 * Represents a mouse click event originating from a client-side interface.
 * This event contains metadata consistent with the JavaScript {@code MouseEvent} interface,
 * including screen, page, and client coordinates, mouse button information,
 * and modifier key states. It is designed for compact and efficient event transmission
 * in client-server UI frameworks.
 *
 * <p>Coordinate pairs (X, Y) are packed into a single 32-bit integer using two 16-bit signed values.
 * This allows transmission of negative positions while reducing overall memory footprint.
 *
 * <p>Modifier keys such as Ctrl, Shift, Alt, and Meta are encoded using bit flags in a single
 * integer field called {@code modifierFlags}, accessible via utility methods such as {@link #isCtrlPressed()}.
 *
 * <p>This class extends {@link Event}, making it compatible with systems expecting
 * general-purpose infrastructure event types.
 *
 * @author Albert
 * @version 1.3
 * @since April 19, 2025
 */
public class ClickEvent extends Event {

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

    private final MouseButton button;
    private final int clientCoords;
    private final int pageCoords;
    private final int screenCoords;
    private final int modifierFlags;

    /**
     * Constructs a {@code ClickEvent} with packed coordinate pairs and modifier flags.
     * All coordinates are signed 16-bit values, packed into a 32-bit integer.
     * Modifier keys are encoded using a bitmask.
     *
     * @param button        the mouse button pressed
     * @param clientX       the X-coordinate of the click within the viewport (signed 16-bit)
     * @param clientY       the Y-coordinate of the click within the viewport (signed 16-bit)
     * @param pageX         the X-coordinate of the click relative to the full document (signed 16-bit)
     * @param pageY         the Y-coordinate of the click relative to the full document (signed 16-bit)
     * @param screenX       the X-coordinate of the click relative to the user's screen (signed 16-bit)
     * @param screenY       the Y-coordinate of the click relative to the user's screen (signed 16-bit)
     * @param modifierFlags bitmask representing which modifier keys were pressed
     */
    public ClickEvent(MouseButton button, int clientX, int clientY, int pageX, int pageY, int screenX, int screenY, int modifierFlags) {
        this.button = button;
        this.clientCoords = packSigned(clientX, clientY);
        this.pageCoords = packSigned(pageX, pageY);
        this.screenCoords = packSigned(screenX, screenY);
        this.modifierFlags = modifierFlags;
    }

    /**
     * Packs two signed 16-bit integers into a single 32-bit integer.
     *
     * @param x the signed X-coordinate
     * @param y the signed Y-coordinate
     * @return a packed 32-bit integer containing both values
     */
    private static int packSigned(int x, int y) {
        return ((x & 0xFFFF) << 16) | (y & 0xFFFF);
    }

    /**
     * Unpacks the signed X-coordinate from a 32-bit packed coordinate pair.
     *
     * @param packed the packed 32-bit coordinate
     * @return the signed X-coordinate
     */
    private static int unpackSignedX(int packed) {
        return (short) (packed >> 16);
    }

    /**
     * Unpacks the signed Y-coordinate from a 32-bit packed coordinate pair.
     *
     * @param packed the packed 32-bit coordinate
     * @return the signed Y-coordinate
     */
    private static int unpackSignedY(int packed) {
        return (short) (packed & 0xFFFF);
    }

    /**
     * Returns the X-coordinate of the click within the viewport.
     *
     * @return signed client X-coordinate
     */
    public int getClientX() {
        return unpackSignedX(clientCoords);
    }

    /**
     * Returns the Y-coordinate of the click within the viewport.
     *
     * @return signed client Y-coordinate
     */
    public int getClientY() {
        return unpackSignedY(clientCoords);
    }

    /**
     * Returns the X-coordinate of the click relative to the full document.
     *
     * @return signed page X-coordinate
     */
    public int getPageX() {
        return unpackSignedX(pageCoords);
    }

    /**
     * Returns the Y-coordinate of the click relative to the full document.
     *
     * @return signed page Y-coordinate
     */
    public int getPageY() {
        return unpackSignedY(pageCoords);
    }

    /**
     * Returns the X-coordinate of the click relative to the user's screen.
     *
     * @return signed screen X-coordinate
     */
    public int getScreenX() {
        return unpackSignedX(screenCoords);
    }

    /**
     * Returns the Y-coordinate of the click relative to the user's screen.
     *
     * @return signed screen Y-coordinate
     */
    public int getScreenY() {
        return unpackSignedY(screenCoords);
    }

    /**
     * Returns the mouse button that triggered the event.
     *
     * @return mouse button value
     */
    public MouseButton getButton() {
        return button;
    }

    /**
     * Returns the raw modifier bitmask, which encodes the state of Ctrl, Shift, Alt, and Meta keys.
     *
     * @return an integer bitmask of pressed modifier keys
     */
    public int getModifierFlags() {
        return modifierFlags;
    }

    /**
     * Returns whether the Control key was pressed during the click.
     *
     * @return true if the Ctrl key was held down
     */
    public boolean isCtrlPressed() {
        return (modifierFlags & CTRL) != 0;
    }

    /**
     * Returns whether the Shift key was pressed during the click.
     *
     * @return true if the Shift key was held down
     */
    public boolean isShiftPressed() {
        return (modifierFlags & SHIFT) != 0;
    }

    /**
     * Returns whether the Alt key was pressed during the click.
     *
     * @return true if the Alt key was held down
     */
    public boolean isAltPressed() {
        return (modifierFlags & ALT) != 0;
    }

    /**
     * Returns whether the Meta key (Windows/Command) was pressed during the click.
     *
     * @return true if the Meta key was held down
     */
    public boolean isMetaPressed() {
        return (modifierFlags & META) != 0;
    }
}