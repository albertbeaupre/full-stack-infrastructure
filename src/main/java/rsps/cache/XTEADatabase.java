package rsps.cache;

import backend.database.chunk.ChunkDatabase;

import java.nio.ByteBuffer;

public class XTEADatabase extends ChunkDatabase<Integer, XTEA> {

    public XTEADatabase() {
        super("./xteas", 4, 16, 1);
    }

    @Override
    protected XTEA deconstructData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return new XTEA(new int[]{buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt()});
    }

    @Override
    protected byte[] constructData(XTEA data) {
        int[] keys = data.keys();
        return ByteBuffer
                .allocate(16)
                .putInt(keys[0])
                .putInt(keys[1])
                .putInt(keys[2])
                .putInt(keys[3]).array();
    }

    @Override
    protected byte[] constructKey(Integer key) {
        return ByteBuffer.allocate(4).putInt(key).array();
    }

    @Override
    protected Integer deconstructKey(byte[] data) {
        return ByteBuffer.wrap(data).getInt();
    }
}
