package PACKAGE_NAME;

public CLASS_NAMEProxy extends PACKAGE_NAME.CLASS_NAME {

    private java.nio.ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);

    public CLASS_NAMEProxy() {
        super();
    }

    public byte[] data() {
        return buffer.array();
    }

    public void load(byte[] data) {
        buffer = ByteBuffer.wrap(array);
    }

}