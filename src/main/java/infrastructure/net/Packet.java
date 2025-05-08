package infrastructure.net;

import infrastructure.io.DynamicByteBuffer;

public class Packet extends DynamicByteBuffer {

    private byte opcode;

    public Packet(byte[] buffer) {
        super(buffer);
    }

    public Packet(int opcode) {
        this(opcode, 0);
    }

    public Packet(int opcode, int length) {
        this(new byte[length]);
        this.writeByte(this.opcode = (byte) opcode);
    }

    public Packet() {
        this(-1, 0);
    }

    public boolean isRaw() {
        return opcode == -1;
    }

}
