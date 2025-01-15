package rsps.network;

/**
 * A {@code Packet} is a message sent to or received from the client that
 * contains an opcode and a buffer of data.
 *
 * @author Albert Beaupre
 */
public class Packet extends RSStream {

    /**
     * The opcode indicating this {@code Packet} as raw. Raw means that an opcode
     * is not associated with the packet.
     */
    public static final byte RAW_OPCODE = -1;

    /**
     * The standard header type
     */
    public static final byte STANDARD = 0;

    /**
     * The byte header type
     */
    public static final byte VAR_BYTE = 1;

    /**
     * The short header type
     */
    public static final byte VAR_SHORT = 2;

    private final byte header;
    private final int opcode;

    /**
     * Constructs an {@code Packet} with the given {@code buffer} as the data for
     * this frame.
     *
     * @param opcode the opcode identifier
     * @param buffer the data buffer
     * @param header the header type
     */
    public Packet(int opcode, byte[] buffer, byte header) {
        super(buffer);
        this.opcode = opcode;
        this.header = header;
    }

    /**
     * Constructs an {@code Packet} with the given {@code buffer} as the data for
     * this frame. The header for this {@code Packet} will be set to
     * {@value #STANDARD} and the opcode will be set to -1;
     *
     * @param buffer the data buffer
     */
    public Packet(byte[] buffer) {
        this(RAW_OPCODE, buffer, STANDARD);
    }

    /**
     * Constructs an {@code Packet} with an empty buffer and the given
     * {@code opcode} as its opcode. The header for this {@code Packet} will be set
     * to {@value #STANDARD}
     *
     * @param opcode the opcode identifier
     */
    public Packet(int opcode) {
        this(opcode, new byte[0], STANDARD);
    }

    /**
     * Constructs an {@code Packet} with an empty buffer.
     *
     * @param opcode the opcode identifier
     * @param header the header type
     */
    public Packet(int opcode, byte header) {
        this(opcode, new byte[0], header);
    }

    /**
     * Constructs an {@code Packet} with an empty buffer and a raw opcode.
     */
    public Packet() {
        this(RAW_OPCODE);
    }

    /**
     * Returns the opcode of this {@code Packet}.
     *
     * @return the opcode
     */
    public int opcode() {
        return this.opcode;
    }

    /**
     * Returns the header type of this {@code Packet}.
     *
     * @return the header
     */
    public byte header() {
        return header;
    }

    /**
     * Returns true if this {@code Packet} is raw.
     *
     * @return true if raw
     */
    public boolean raw() {
        return (this.opcode == -1);
    }

}
