package infrastructure.io;

public interface ByteSerializable {

    void load(byte[] data);

    byte[] data();

}
