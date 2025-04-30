package infrastructure.web.ui;

/**
 * The {@code ComponentType} enum defines common UI components,
 * each assigned a unique 4-bit binary identifier (0–15) and
 * a corresponding string name (e.g., "submit", "email").
 *
 * <p>This enum is primarily used in systems that encode component and event
 * data into a single byte, where the high 4 bits represent the component
 * (from this enum) and the low 4 bits represent an {@link EventType}.
 *
 * <p>Each constant includes both a binary ID (for packed transmission) and
 * a human-readable component name (for logs, routing, or frontend generation).
 *
 * <p>Example usage:
 * <pre>{@code
 *     byte packed = (ComponentType.EMAIL_FIELD.getBinary() << 4) | EventType.FOCUS.getBinary();
 *     ComponentType type = ComponentType.fromBinary((packed >> 4) & 0x0F);
 * }</pre>
 */
public enum ComponentType {

    /** Represents a "Submit" button component. */
    SUBMIT_BUTTON(0b0001, "submit"),

    /** Represents a "Cancel" button component. */
    CANCEL_BUTTON(0b0010, "cancel"),

    /** Represents a text input for "Name". */
    NAME_FIELD(0b0011, "name"),

    /** Represents an "Email" input field. */
    EMAIL_FIELD(0b0100, "email"),

    /** Represents a "Password" input field. */
    PASSWORD_FIELD(0b0101, "password"),

    /** Represents a checkbox for terms or agreement. */
    AGREE_CHECKBOX(0b0110, "agree"),

    /** Represents a select/dropdown input for roles. */
    ROLE_SELECTOR(0b0111, "role"),

    /** Represents a radio button group for gender. */
    GENDER_RADIO(0b1000, "gender"),

    /** Represents a slider input for volume. */
    VOLUME_SLIDER(0b1001, "volume"),

    /** Represents a textarea input for comments. */
    COMMENT_AREA(0b1010, "comment");

    /**
     * The 4-bit binary identifier (0–15), intended for use in compact
     * byte-packing formats where it occupies the high nibble of a byte.
     */
    private final int binary;

    /**
     * The human-readable name of the component, used for logging,
     * debugging, or serialization.
     */
    private final String componentName;

    /**
     * Constructs a {@code ComponentType} with the given 4-bit binary identifier
     * and component name.
     *
     * @param binary         a 4-bit integer (0–15) unique to this component
     * @param componentName  a human-readable name (e.g., "submit")
     * @throws IllegalArgumentException if the binary value exceeds 4 bits
     */
    ComponentType(int binary, String componentName) {
        if ((binary & 0xF0) != 0)
            throw new IllegalArgumentException("ComponentId must be 4 bits max (0–15)");

        this.binary = binary;
        this.componentName = componentName;
    }

    /**
     * Returns the 4-bit binary ID for this component.
     * Used for packing into the high nibble of a byte.
     *
     * @return the binary value (0–15)
     */
    public int getBinary() {
        return binary;
    }

    /**
     * Returns the human-readable component name (e.g., "email", "submit").
     *
     * @return the component name
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Resolves a {@code ComponentType} from its 4-bit binary representation.
     *
     * @param value the binary ID (0–15)
     * @return the matching {@code ComponentType}, or {@code null} if unknown
     */
    public static ComponentType fromBinary(int value) {
        for (ComponentType id : values()) {
            if (id.binary == value) {
                return id;
            }
        }
        return null;
    }
}