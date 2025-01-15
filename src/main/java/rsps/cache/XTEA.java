package rsps.cache;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The XTEA (XTEA is an acronym for Extended Tiny Encryption Algorithm) class
 * represents a simple implementation of the XTEA block cipher. It operates
 * on 64-bit blocks of data and employs a Feistel network structure with a
 * fixed number of rounds.
 * <p>
 * XTEA is a symmetric-key block cipher designed for fast and secure encryption
 * of data. This class provides methods for both enciphering and deciphering
 * data using the XTEA algorithm.
 * <p>
 * The XTEA algorithm is parameterized by a 128-bit key, represented as an
 * array of four integers. The class offers a default empty key (all zeros)
 * and exposes methods for both enciphering and deciphering data using the
 * specified key.
 */
public record XTEA(int[] keys) {

    /**
     * An instance of XTEA with an empty key (all zeros).
     */
    public static final XTEA Empty = new XTEA(new int[]{0, 0, 0, 0});

    /**
     * The golden ratio constant used in the XTEA algorithm.
     */
    public static final int GoldenRatio = 0x9E3779B9;

    /**
     * The number of rounds in the XTEA algorithm.
     */
    public static final int Rounds = 32;

    /**
     * Checks if the key is an empty key (all zeros).
     *
     * @return true if the key is empty, false otherwise.
     */
    private boolean isZeroed() {
        for (int i = 0; i < keys.length; i++)
            if (keys[i] != 0)
                return false;
        return true;
    }

    /**
     * Deciphers a specified range of bytes in the given ByteBuffer using the
     * XTEA algorithm.
     *
     * @param buffer the ByteBuffer containing the data to be deciphered.
     * @param start  the start index of the data range.
     * @param end    the end index of the data range.
     */
    public void decipher(ByteBuffer buffer, int start, int end) {
        if (isZeroed())
            return;

        int numQuads = (end - start) / 8;
        for (int i = 0; i < numQuads; i++) {
            int sum = GoldenRatio * Rounds;
            int v0 = buffer.getInt(start + i * 8);
            int v1 = buffer.getInt(start + i * 8 + 4);
            for (int j = 0; j < Rounds; j++) {
                v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + keys[(sum >>> 11) & 3]);
                sum -= GoldenRatio;
                v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + keys[sum & 3]);
            }
            buffer.putInt(start + i * 8, v0);
            buffer.putInt(start + i * 8 + 4, v1);
        }
    }

    /**
     * Enciphers a specified range of bytes in the given ByteBuffer using the
     * XTEA algorithm.
     *
     * @param buffer the ByteBuffer containing the data to be enciphered.
     * @param start  the start index of the data range.
     * @param end    the end index of the data range.
     */
    public void encipher(ByteBuffer buffer, int start, int end) {
        if (isZeroed())
            return;

        int numQuads = (end - start) / 8;
        for (int i = 0; i < numQuads; i++) {
            int sum = 0;
            int v0 = buffer.getInt(start + i * 8);
            int v1 = buffer.getInt(start + i * 8 + 4);
            for (int j = 0; j < Rounds; j++) {
                v0 += (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + keys[sum & 3]);
                sum += GoldenRatio;
                v1 += (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + keys[(sum >>> 11) & 3]);
            }
            buffer.putInt(start + i * 8, v0);
            buffer.putInt(start + i * 8 + 4, v1);
        }
    }

    /**
     * Returns a string representation of the XTEA object.
     *
     * @return a string in the format "XTEA[key1, key2, key3, key4]".
     */
    @Override
    public String toString() {
        return STR."XTEA[\{Arrays.toString(keys)}]";
    }
}