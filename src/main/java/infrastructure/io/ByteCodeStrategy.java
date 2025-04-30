package infrastructure.io;

/**
 * Strategy interface for converting between generic byte‐code objects and raw byte arrays.
 *
 * <p>This interface defines a contract for serializing (constructing) a byte‐code object of type B
 * into a {@code byte[]} suitable for storage or transmission, and for deserializing
 * (deconstructing) that {@code byte[]} back into the original byte‐code object.
 * Typical uses include persisting chunks of a database to disk, sending them over the network,
 * or applying additional layers such as compression, encryption, or versioning without
 * changing the core chunk management logic.
 *
 * <h3>Implementation Requirements</h3>
 * <ul>
 *   <li>Implementations must ensure that for any non‐null {@code byteCode}:
 *       <pre>
 *           deconstruct(construct(byteCode)).equals(byteCode)
 *       </pre>
 *       (i.e., the process is reversible).</li>
 *   <li>Implementations should be stateless and thread‐safe unless otherwise documented.</li>
 *   <li>Implementations may embed metadata (e.g., headers, checksums, version tags)
 *       in the serialized form to support integrity checks and backward compatibility.</li>
 * </ul>
 *
 * @param <B> the type of the byte‐code object being handled
 */
public interface ByteCodeStrategy<B> {

    /**
     * Serializes the given byte‐code object into a raw byte array.
     *
     * <p>The returned array should contain all information necessary to fully reconstruct
     * the original object, including any metadata required by {@link #deconstruct(byte[])}.
     * Implementations may choose to compress, encrypt, or otherwise transform the data,
     * provided the transformation is fully reversible.
     *
     * @param byteCode the byte‐code object to serialize; must not be {@code null}
     * @return a non‐null {@code byte[]} containing the serialized form
     * @throws IllegalArgumentException if {@code byteCode} is {@code null} or cannot be serialized
     * @throws IllegalStateException if an unexpected error occurs during serialization
     */
    byte[] construct(B byteCode);

    /**
     * Deserializes the given raw byte array back into a byte‐code object.
     *
     * <p>The input array must conform to the format produced by {@link #construct(Object)}.
     * Implementations should validate any embedded metadata (such as checksums or version tags)
     * and throw an exception if the data is corrupted or incompatible.
     *
     * @param data the byte array to deserialize; must not be {@code null}
     * @return a new instance of {@code B} reconstructed from the input data
     * @throws IllegalArgumentException if {@code data} is {@code null} or has invalid format
     * @throws IllegalStateException if the data is corrupted or fails integrity checks
     */
    B deconstruct(byte[] data);
}
