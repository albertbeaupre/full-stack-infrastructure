package infrastructure.net.web.ui;

/**
 * Represents the mouse button used in a click event.
 * Mirrors standard browser button values from JavaScript's {@code MouseEvent.button}:
 * <ul>
 *     <li>{@code LEFT} = 0</li>
 *     <li>{@code MIDDLE} = 1</li>
 *     <li>{@code RIGHT} = 2</li>
 *     <li>{@code BACK} = 3 (typically browser back button)</li>
 *     <li>{@code FORWARD} = 4 (typically browser forward button)</li>
 * </ul>
 *
 * <p>Use {@link #fromCode(int)} to convert the raw browser button code into a corresponding enum value.
 *
 * @author Albert
 * @since April 19, 2025
 */
public enum MouseButton {

    LEFT(0),
    MIDDLE(1),
    RIGHT(2),
    BACK(3),
    FORWARD(4);

    private final int code;

    MouseButton(int code) {
        this.code = code;
    }

    /**
     * Gets the numeric browser-compatible code of this button.
     *
     * @return the raw code (0 for left, 1 for middle, etc.)
     */
    public int getCode() {
        return code;
    }

    /**
     * Converts a raw integer button code into a {@code MouseButton} enum.
     *
     * @param code the button code (0–4)
     * @return the corresponding {@code MouseButton}, or {@code null} if unknown
     */
    public static MouseButton fromCode(int code) {
        for (MouseButton button : values()) {
            if (button.code == code) {
                return button;
            }
        }
        return null;
    }
}
