package rsps.network;

import java.util.Arrays;
import java.util.function.Function;

/**
 * The {@code RSStream} class allows for easy reading and writing from a byte
 * buffer that contains data used by anything relating to the RuneScape cache or
 * network.
 *
 * @author Albert Beaupre
 */
public class RSStream {

    protected byte[] buffer; // the bytes holding all data
    private int readerIndex; // the index at when reading the buffer
    private int writerIndex; // the index at when writing to the buffer
    private int bitIndex; // the index at when writing bits to the buffer

    /**
     * Constructs a new {@code RStream} with the given {@code buffer} as the buffer
     * for reading and writing to/from.
     *
     * @param buffer the buffer to set
     */
    public RSStream(byte[] buffer) {
        if (buffer == null)
            throw new NullPointerException("Cannot construct a new RSStream with a null buffer");
        this.buffer = buffer;
    }

    /**
     * Constructs an empty {@code RSStream} with a default capacity from the provided {@code lengh}.
     */
    public RSStream(int length) {
        this(new byte[length]);
    }

    /**
     * Constructs an empty {@code RSStream} with a default capacity of 256
     */
    public RSStream() {
        this(new byte[256]);
    }

    /**
     * Checks for reader and writer index issues
     */
    private final void check(boolean reading, boolean writing, int lengthCheck) {
        if (reading && readerIndex + lengthCheck >= buffer.length)
            throw new IndexOutOfBoundsException(STR."reader index \{readerIndex + lengthCheck} is greater than buffer size \{buffer.length}. no more bytes to read");

        if (writing && writerIndex + lengthCheck >= buffer.length)
            buffer = Arrays.copyOf(buffer, writerIndex + lengthCheck);
    }

    /**
     * Returns the byte read in the buffer at the current reader index and increases
     * the reader index by 1.
     *
     * @return the byte read
     */
    public byte readByte() {
        check(true, false, 0);
        return buffer[readerIndex++];
    }

    /**
     * Returns the a-type byte read in the buffer at the current reader index and
     * increases the reader index by 1.
     *
     * @return the byte read
     */
    public byte readByteA() {
        return (byte) (readByte() - 128);
    }

    /**
     * Returns the unsigned byte read in the buffer at the current reader index and
     * increases the reader index by 1.
     *
     * @return the unsigned byte read
     */
    public int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    /**
     * Reads the bytes in the buffer at the current reader index through to the
     * given {@code length} and returns the bytes within that space.
     *
     * @param length the length of the bytes to read
     * @return the array with the bytes read
     */
    public byte[] readBytes(int length) {
        check(true, false, length);
        byte[] array = Arrays.copyOfRange(buffer, readerIndex, readerIndex + length);
        readerIndex += length;
        return array;
    }

    /**
     * Reads the bytes in the buffer at the current reader index through to the
     * given {@code length} and returns the bytes within that space.
     */
    public void readBytesInto(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) readByte();
        }
    }

    /**
     * Returns the short read in the buffer at the current reader index and
     * increases the reader index by 2.
     *
     * @return the short read
     */
    public int readShort(int index) {
        return ((getByte(index) & 0xFF) << 8) | (getByte(index + 1) & 0xFF);
    }

    /**
     * Returns the short read in the buffer at the current reader index and
     * increases the reader index by 2.
     *
     * @return the short read
     */
    public short readShort() {
        return (short) ((readUnsignedByte() << 8) | (readUnsignedByte()));
    }

    /**
     * Returns the unsigned short read in the buffer at the current reader index and
     * increases the reader index by 2.
     *
     * @return the unsigned short read
     */
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    /**
     * Returns the a-type short read in the buffer at the current reader index and
     * increases the reader index by 2.
     *
     * @return the a-type short read
     */
    public int readShortA() {
        return (readUnsignedByte() << 8) | (readByte() - 128 & 0xFF);
    }

    /**
     * Returns the little-endian short read in the buffer at the current reader
     * index and increases the reader index by 2.
     *
     * @return the little-endian short read
     */
    public int readLEShort() {
        return readUnsignedByte() | (readUnsignedByte() << 8);
    }

    /**
     * Returns the little-endian a-type short read in the buffer at the current
     * reader index and increases the reader index by 2.
     *
     * @return the little-endian a-type short read
     */
    public int readLEShortA() {
        return (readByte() - 128 & 0xFF) | (readUnsignedByte() << 8);
    }

    /**
     * Returns the medium read in the buffer at the current reader index and
     * increases the reader index by 2.
     *
     * @return the medium short read
     */
    public int readMedium() {
        return (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | (readUnsignedByte());
    }

    /**
     * Returns the int read in the buffer at the current reader index and increases
     * the reader index by 4.
     *
     * @return the int read
     */
    public int readInt() {
        return (readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | readUnsignedByte();
    }

    /**
     * Returns the int read in the buffer at the current reader index and increases
     * the reader index by 4.
     *
     * @return the int read
     */
    public int readInt(int index) {
        return ((getByte(index) & 0xFF) << 24) | ((getByte(index + 1) & 0xFF) << 16) | ((getByte(index + 1) & 0xFF) << 8) | getByte(index + 3) & 0xFF;
    }

    /**
     * Returns the unsigned int read in the buffer at the current reader index and
     * increases the reader index by 4. This value is read by the 3rd byte first,
     * the 4th byte second, the 1st byte third and the 2nd byte fourth.
     *
     * @return the unsigned int read
     */
    public int readInt1() {
        int unsignedFirst = readUnsignedByte();
        int unsignedSecond = readUnsignedByte();
        int unsignedThird = readUnsignedByte();
        int unsignedFourth = readUnsignedByte();
        return (unsignedThird << 24 | unsignedFourth << 16 | unsignedFirst << 8 | unsignedSecond);
    }

    /**
     * Returns the unsigned int read in the buffer at the current reader index and
     * increases the reader index by 4. This value is read by the 2nd byte first,
     * the 1st byte second, the 4th byte third and the 3rd byte fourth.
     *
     * @return the unsigned int read
     */
    public int readInt2() {
        int unsignedFirst = readUnsignedByte();
        int unsignedSecond = readUnsignedByte();
        int unsignedThird = readUnsignedByte();
        int unsignedFourth = readUnsignedByte();
        return (unsignedSecond << 24 | unsignedFirst << 16 | unsignedFourth << 8 | unsignedThird);
    }

    /**
     * Returns the little-endian int read in the buffer at the current reader index
     * and increases the reader index by 4.
     *
     * @return the little-endian int read
     */
    public int readLEInt() {
        return readUnsignedByte() + (readUnsignedByte() << 8) + (readUnsignedByte() << 16) + (readUnsignedByte() << 24);
    }

    /**
     * Returns the unsigned int read in the buffer at the current reader index and
     * increases the reader index by 4.
     *
     * @return the unsigned int read
     */
    public long readUnsignedInt() {
        return readInt() & 0xFFFFFFFFL;
    }

    /**
     * Returns the little-endian int read in the buffer at the current reader index
     * and increases the reader index by 4.
     *
     * @return the little-endian int read
     */
    public int readLittleEndianInt() {
        return readUnsignedByte() | (readUnsignedByte() << 8) | (readUnsignedByte() << 16) | (readUnsignedByte() << 24);
    }

    /**
     * Returns the float read in the buffer at the current reader index and
     * increases the reader index by 4.
     *
     * @return the float read
     */
    public float readFloat() {
        int intBits = readByte() << 24 | readUnsignedByte() << 16 | readUnsignedByte() << 8 | readUnsignedByte();
        return Float.intBitsToFloat(intBits);
    }

    /**
     * Returns the long read in the buffer at the current reader index and increases
     * the reader index by 8.
     *
     * @return the long read
     */
    public long readLong() {
        return (readUnsignedByte() << 56) | (readUnsignedByte() << 48) | (readUnsignedByte() << 40) | (readUnsignedByte() << 32) | (readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | readUnsignedByte();
    }

    /**
     * Returns the smart read in the buffer at the current reader index and
     * increases the reader index by 2 if a short was read or 1 if a byte was read.
     *
     * @return the smart read
     */
    public int readSmart() {
        int value = getByte(readerIndex);
        if (value <= Byte.MAX_VALUE) {
            return readUnsignedByte();
        } else {
            return readUnsignedShort() - 32768;
        }
    }

    public int readExtendedSmart() {
        int total = 0;
        int smart = readUnsignedSmart();
        while (smart == 0x7FFF) {
            smart = readUnsignedSmart();
            total += 0x7FFF;
        }
        total += smart;
        return total;
    }

    /**
     * Returns the unsigned smart read in the buffer at the current reader index and
     * increases the reader index by 2 if a short was read or 1 if a byte was read.
     *
     * @return the unsigned smart read
     */
    public int readUnsignedSmart() {
        int value = getByte(readerIndex) & 0xFF;
        if (value <= Byte.MAX_VALUE) {
            return readByte();
        } else {
            return readUnsignedShort() - 32768;
        }
    }

    /**
     * Returns the string read in the buffer at the current reader index. The reader
     * index is increased until the next byte found equals 0.
     *
     * @return the string read
     */
    public String readRSString() {
        StringBuilder result = new StringBuilder();
        int characterRead;
        while ((characterRead = readByte()) != 0)
            result.append((char) characterRead);
        return result.toString();
    }

    /**
     * Sets the bit index based on the current writer index. This method should be
     * called before calling {@link #writeBits(int, int)} .
     *
     * @return this current instance, used for chaining
     */
    public RSStream beginBitAccess() {
        bitIndex = writerIndex * Byte.SIZE;
        return this;
    }

    /**
     * Sets the writer index based on the current bit index. The method should be
     * called once finishing calling {@link #writeBits(int, int)}.
     *
     * @return this current instance, used for chaining
     */
    public RSStream finishBitAccess() {
        writerIndex = (bitIndex + 7) / Byte.SIZE;
        return this;
    }

    /**
     * Skips bytes equal to the provided {@code length}.
     */
    public void skip(int length) {
        this.readerIndex += Math.min(length, readableBytes());
        this.writerIndex += length;
    }

    /**
     * Writes the given {@code value} with the {@code numBits}.
     *
     * @param numBits the number of bits
     * @param value   the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeBits(int numBits, int value) {
        check(false, true, numBits);
        Function<Integer, Integer> bitMask = index -> (1 << index) - 1;

        int bytePos = bitIndex >> 3;
        int bitOffset = 8 - (bitIndex & 7);

        bitIndex += numBits;
        writerIndex = (bitIndex + 7) / Byte.SIZE;

        byte b;
        for (; numBits > bitOffset; bitOffset = Byte.SIZE) {
            b = (byte) getByte(bytePos);
            setByte(bytePos, (byte) (b & ~bitMask.apply(bitOffset)));
            setByte(bytePos++, (byte) (b | (value >> (numBits - bitOffset)) & bitMask.apply(bitOffset)));
            numBits -= bitOffset;
        }
        b = (byte) getByte(bytePos);
        if (numBits == bitOffset) {
            setByte(bytePos, (byte) (b & ~bitMask.apply(bitOffset)));
            setByte(bytePos, (byte) (b | value & bitMask.apply(bitOffset)));
        } else {
            setByte(bytePos, (byte) (b & ~(bitMask.apply(numBits) << (bitOffset - numBits))));
            setByte(bytePos, (byte) (b | (value & bitMask.apply(numBits)) << (bitOffset - numBits)));
        }
        return this;
    }

    /**
     * Writes the byte {@code value} to the buffer at the current writer index and
     * increases the writer index by 1.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeByte(long value) {
        check(false, true, 1);
        buffer[writerIndex++] = (byte) value;
        return this;
    }

    /**
     * Writes the byte as 128 - {@code value} to the buffer at the current writer
     * index and increases the writer index by 1.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeByteS(int value) {
        return writeByte((byte) (128 - value));
    }

    /**
     * Writes the byte as 128 - {@code value} to the buffer at the current writer
     * index and increases the writer index by 1.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeByteA(int value) {
        return writeByte(128 + value);
    }

    /**
     * Writes the given {@code bytes} array to the buffer at the current writer
     * index and increases the writer index by the length of the array.
     *
     * @param bytes the array to write
     * @return this current instance, used for chaining
     */
    public RSStream writeBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            writeByte(bytes[i]);
        }
        return this;
    }

    public RSStream writeBytes(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length; i++) {
            writeByte(bytes[i]);
        }
        return this;
    }

    /**
     * Writes the bytes from the given {@code stream} to this {@code RSStream}.
     *
     * @param stream the stream to transfer the bytes from
     * @return this current instance, used for chaining
     */
    public RSStream writeBytes(RSStream stream) {
        return writeBytes(stream.writerArray());
    }

    public RSStream writeByteC(int value) {
        return writeByte(-value);
    }

    /**
     * Writes the short {@code value} to the buffer at the current writer index and
     * increases the writer index by 2.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeShort(int value) {
        return writeByte(value >> 8).writeByte(value);
    }

    /**
     * Writes the a-type short {@code value} to the buffer at the current writer
     * index and increases the writer index by 2.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeShortA(int value) {
        return writeByte((byte) (value >> 8)).writeByte((byte) (value + 128));
    }

    /**
     * Writes the little-endian short {@code value} to the buffer at the current
     * writer index and increases the writer index by 2.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeLEShort(int value) {
        return writeByte((byte) value).writeByte((byte) (value >> 8));
    }

    /**
     * Writes the little-endian a-type short {@code value} to the buffer at the
     * current writer index and increases the writer index by 2.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeLEShortA(int value) {
        return writeByte(value + 128).writeByte(value >> 8);
    }

    /**
     * Writes a 24Bit value by writing 3 bytes. The first byte if shifted to the
     * right by 16, the second byte is shift to the right by 8, and the third byte
     * is the value itself.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeMedium(int value) {
        return writeByte((byte) (value >> 16)).writeByte((byte) (value >> 8)).writeByte((byte) value);
    }

    /**
     * Writes the int {@code value} to the buffer at the current writer index and
     * increases the writer index by 4.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeInt(int value) {
        return writeByte(value >> 24).writeByte(value >> 16).writeByte(value >> 8).writeByte(value);
    }

    /**
     * Writes the int {@code value} to the buffer at the given {@code index}. This
     * does not icrease the writer index.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeInt(int index, int value) {
        return setByte(index, value >> 24).setByte(index + 1, value >> 16).setByte(index + 2, value >> 8).setByte(index + 3, value);
    }

    /**
     * Writes the little-endian int {@code value} to the buffer at the current
     * writer index and increases the writer index by 4.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeLEInt(int value) {
        return writeByte((byte) value).writeByte((byte) (value >> 8)).writeByte((byte) (value >> 16)).writeByte((byte) (value >> 24));
    }

    /**
     * Writes the int {@code value} to the buffer at the current writer index and
     * increases the writer index by 4.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeInt1(int value) {
        return writeByte((byte) (value >> 8)).writeByte((byte) value).writeByte((byte) (value >> 24)).writeByte((byte) (value >> 16));
    }

    /**
     * Writes the int {@code value} to the buffer at the current writer index and
     * increases the writer index by 4.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeInt2(int value) {
        return writeByte((byte) (value >> 16)).writeByte((byte) (value >> 24)).writeByte((byte) value).writeByte((byte) (value >> 8));
    }

    /**
     * Writes the float {@code value} to the buffer at the current writer index and
     * increases the writer index by 4.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeFloat(float value) {
        int intBits = Float.floatToIntBits(value);
        return writeByte(intBits >> 24).writeByte(intBits >> 16).writeByte(intBits >> 8).writeByte(intBits);
    }

    /**
     * Writes the long {@code value} to the buffer at the current writer index and
     * increases the writer index by 8.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeLong(long value) {
        return writeByte(value).writeByte(value >> 8).writeByte(value >> 16).writeByte(value >> 24).writeByte(value >> 32).writeByte(value >> 40).writeByte(value >> 48).writeByte(value >> 56);
    }

    /**
     * Writes the int {@code value} to the buffer at the current writer index and
     * increases the writer index by 2 if the value is > {@link Byte#MAX_VALUE}
     * otherwise the writer index is increased by 1.
     *
     * @param value the value to write
     * @return this current instance, used for chaining
     */
    public RSStream writeSmart(int value) {
        if (value > Byte.MAX_VALUE) {
            return writeShort(value + 32768);
        } else {
            return writeByte(value);
        }
    }

    /**
     * Writes the given {@code string} to the buffer at the current writer index and
     * increases the writer index by the string length plus 1. A byte value of 0 is
     * written after writing the string bytes.
     *
     * @param string the string to write
     * @return this current instance, used for chaining
     */
    public RSStream writeRSString(String string) {
        return writeBytes(string.getBytes()).writeByte((byte) 0);
    }

    /**
     * Writes the given {@code string} to the buffer at the current writer index and
     * increases the writer index by the string length plus 2. A byte value of 0 is
     * written before and after writing the string bytes.
     *
     * @param string the string to write
     * @return this current instance, used for chaining
     */
    public RSStream writeGJString(String string) {
        return writeByte(0).writeBytes(string.getBytes()).writeByte(0);
    }

    /**
     * Sets the byte {@code value} at the given {@code index} in the buffer. This
     * does not change the reader or writer index.
     *
     * @param index the index at set the value at
     * @param value the value to set
     * @return this current instance, used for chaining
     */
    public RSStream setByte(int index, int value) {
        if (index >= buffer.length)
            throw new IndexOutOfBoundsException(STR."cannot set value at index \{index} because buffer size is only \{buffer.length}");
        if (index < 0)
            throw new IndexOutOfBoundsException(STR."cannot set value at negative index \{index}");
        buffer[index] = (byte) value;
        return this;
    }

    /**
     * Returns the byte value at the given {@code index} in this buffer. This does
     * not change the reader or writer index.
     *
     * @param index the index to get the value from
     * @return the value in the buffer at the index
     */
    public int getByte(int index) {
        if (index >= buffer.length)
            throw new IndexOutOfBoundsException("cannot get value at index " + index + " because buffer size is only " + buffer.length);
        if (index < 0)
            throw new IndexOutOfBoundsException("cannot get value at negative index " + index);
        return buffer[index];
    }

    /**
     * The index this {@code RSStream} is currently at when reading from the buffer.
     *
     * @return the reading index
     */
    public int readerIndex() {
        return readerIndex;
    }

    /**
     * Sets the new reader index of this {@code RSStream} to the given
     * {@code readerIndex}.
     *
     * @return this current instance, used for chaining
     */
    public RSStream readerIndex(int readerIndex) {
        this.readerIndex = readerIndex;
        return this;
    }

    /**
     * The index this {@code RSStream} is currently at when writing to the buffer.
     *
     * @return the writing index
     */
    public int writerIndex() {
        return writerIndex;
    }

    /**
     * Sets the new writer index of this {@code RSStream} to the given
     * {@code writerIndex}.
     *
     * @return this current instance, used for chaining
     */
    public RSStream writerIndex(int writerIndex) {
        this.writerIndex = writerIndex;
        return this;
    }

    /**
     * Resets the reader index and writer index by setting them to 0
     */
    public RSStream reset() {
        this.readerIndex = 0;
        this.writerIndex = 0;
        return this;
    }

    /**
     * Returns the amount of bytes left to read within the buffer.
     *
     * @return the amount of bytes left
     */
    public int readableBytes() {
        return buffer.length - readerIndex - 1;
    }

    /**
     * Clears this {@code RSStream} buffer.
     */
    public void clear() {
        Arrays.fill(buffer, (byte) 0);
    }

    /**
     * Returns the capacity of the buffer.
     *
     * @return the capacity
     */
    public int capacity() {
        return buffer.length;
    }

    /**
     * Returns the buffer array in this {@code RSStream} based on where the writer
     * index is.
     *
     * @return the array
     */
    public byte[] writerArray() {
        return Arrays.copyOfRange(buffer, 0, writerIndex);
    }

    /**
     * Returns the buffer array in this {@code RSStream} based on where the reader
     * index is.
     *
     * @return the array
     */
    public byte[] readerArray() {
        return Arrays.copyOfRange(buffer, readerIndex, capacity());
    }

    /**
     * Returns the exact buffer being read from and written to.
     *
     * @return the exact buffer
     */
    public byte[] buffer() {
        return buffer;
    }

}
