package backend.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

/**
 * The `CompressionStrategy` interface defines the contract for classes that implement various compression
 * and decompression algorithms. Classes implementing this interface should provide methods to encode (compress)
 * and decode (decompress) data, typically represented as byte arrays.
 * <p>
 * Implementations of this interface can be used to compress and decompress data in a consistent manner,
 * allowing for flexibility in choosing different compression algorithms while maintaining a common interface.
 * This is particularly useful when dealing with various data storage or transmission scenarios where different
 * compression techniques may be more suitable.
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since 1.0
 */
public interface CompressionStrategy {

    /**
     * The Gzip class implements the CompressionStrategy interface and provides methods
     * for compressing and decompressing data using the GZIP (GNU ZIP) compression algorithm.
     * <p>
     * GZIP is a widely used file compression format that is commonly found in various
     * file formats and protocols, including HTTP compression and compressed archive formats
     * like .gz and .tar.gz. This class uses Java's GZIPOutputStream and GZIPInputStream classes
     * to perform compression and decompression, respectively.
     */
    CompressionStrategy GZIP = new CompressionStrategy() {

        /**
         * Compresses the given byte array using the GZIP compression algorithm.
         *
         * @param data The input data to be compressed.
         * @return The compressed data as a byte array.
         * @throws RuntimeException If an error occurs during compression.
         */
        @Override
        public byte[] compress(byte[] data) {
            try (ByteArrayOutputStream compressed = new ByteArrayOutputStream()) {
                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressed)) {
                    gzipOutputStream.write(data);
                }

                return compressed.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to compress using GZip algorithm", e);
            }
        }

        /**
         * Decompresses the given byte array using the GZIP decompression algorithm.
         *
         * @param data The compressed data to be decompressed.
         * @return The decompressed data as a byte array.
         * @throws RuntimeException If an error occurs during decompression.
         */
        @Override
        public byte[] decompress(byte[] data) {
            try (ByteArrayOutputStream decompressed = new ByteArrayOutputStream()) {
                try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(data))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                        decompressed.write(buffer, 0, bytesRead);
                    }
                }

                return decompressed.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to decompress using GZip algorithm", e);
            }
        }
    };

    /**
     * The Deflate class implements the CompressionStrategy interface and provides methods
     * for compressing and decompressing data using the Deflate compression algorithm.
     * <p>
     * Deflate is a widely used lossless data compression algorithm that is supported by
     * various file formats and protocols, including ZIP files and HTTP compression. This class
     * uses the java.util.zip.Deflater and java.util.zip.Inflater classes to perform compression
     * and decompression, respectively.
     */
    CompressionStrategy Deflate = new CompressionStrategy() {
        final Deflater deflater = new Deflater();
        final Inflater inflater = new Inflater();

        /**
         * Compresses the given byte array using the Deflate compression algorithm.
         *
         * @param data The input data to be compressed.
         * @return The compressed data as a byte array.
         * @throws RuntimeException If an error occurs during compression.
         */
        @Override
        public byte[] compress(byte[] data) {
            Deflater deflater = new Deflater();
            deflater.setInput(data);
            deflater.finish();

            // Allocate a ByteBuffer with an initial capacity of half the input data length.
            ByteBuffer compressedBuffer = ByteBuffer.allocate(data.length / 2);

            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int compressedBytes = deflater.deflate(buffer);

                // Ensure there is enough space in the ByteBuffer
                if (compressedBuffer.remaining() < compressedBytes) {
                    // Double the capacity of the ByteBuffer if needed
                    ByteBuffer newBuffer = ByteBuffer.allocate(compressedBuffer.capacity() * 2);
                    compressedBuffer.flip(); // Prepare the buffer to be read
                    newBuffer.put(compressedBuffer); // Transfer the data to the new buffer
                    compressedBuffer = newBuffer;
                }

                compressedBuffer.put(buffer, 0, compressedBytes);
            }
            deflater.end();

            // Prepare the ByteBuffer to be read
            compressedBuffer.flip();

            // Create the final byte array with the exact compressed size
            byte[] compressedData = new byte[compressedBuffer.remaining()];
            compressedBuffer.get(compressedData);
            return compressedData;
        }

        /**
         * Decompresses the given byte array using the Deflate decompression algorithm.
         *
         * @param data The compressed data to be decompressed.
         * @return The decompressed data as a byte array.
         * @throws RuntimeException If an error occurs during decompression.
         */
        @Override
        public byte[] decompress(byte[] data) {
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            try {
                // Allocate a ByteBuffer with an initial capacity of twice the input data length.
                ByteBuffer decompressedBuffer = ByteBuffer.allocate(data.length * 2);

                byte[] buffer = new byte[1024];
                while (!inflater.finished()) {
                    int decompressedBytes = inflater.inflate(buffer);

                    // Ensure there is enough space in the ByteBuffer
                    if (decompressedBuffer.remaining() < decompressedBytes) {
                        // Double the capacity of the ByteBuffer if needed
                        ByteBuffer newBuffer = ByteBuffer.allocate(decompressedBuffer.capacity() * 2);
                        decompressedBuffer.flip(); // Prepare the buffer to be read
                        newBuffer.put(decompressedBuffer); // Transfer the data to the new buffer
                        decompressedBuffer = newBuffer;
                    }

                    decompressedBuffer.put(buffer, 0, decompressedBytes);
                }
                inflater.end();

                // Prepare the ByteBuffer to be read
                decompressedBuffer.flip();

                // Create the final byte array with the exact decompressed size
                byte[] decompressedData = new byte[decompressedBuffer.remaining()];
                decompressedBuffer.get(decompressedData);

                return decompressedData;
            } catch (Exception e) {
                throw new RuntimeException("Failed to decompress using Deflate algorithm", e);
            }
        }
    };

    /**
     * Compresses the input data represented as a byte array.
     *
     * @param data The input data to be compressed.
     * @return A byte array containing the compressed data.
     */
    byte[] compress(byte[] data);

    /**
     * Decompresses the input data represented as a byte array.
     *
     * @param data The input data to be decompressed.
     * @return A byte array containing the decompressed data.
     */
    byte[] decompress(byte[] data);


}

