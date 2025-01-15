package backend.authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code User} class represents a user with a set of attributes that can be used
 * in the authentication process or for storing user-specific data. This class provides
 * methods to add, retrieve, and access user attributes in a flexible manner.
 *
 * <p>This class utilizes a map to store attributes, which allows for dynamic attribute
 * management without needing predefined fields. The attributes are stored as key-value
 * pairs, where keys are strings and values are objects of any type.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * User user = new User()
 *     .attribute("username", "john_doe")
 *     .attribute("age", 30);
 *
 * String username = user.get("username", String.class);
 * Integer age = user.get("age", Integer.class, 0);
 * }</pre>
 *
 * @author Albert Beaupre
 * @since September 4th, 2024
 */
public class User {

    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Adds or updates an attribute with the specified key and value.
     *
     * <p>This method allows adding a new attribute or updating an existing one. It
     * returns the current instance of {@code User} to facilitate method chaining.</p>
     *
     * @param key the key for the attribute.
     * @param object the value of the attribute.
     * @return the current {@code User} instance.
     */
    public User attribute(String key, Object object) {
        this.attributes.put(key, object);
        return this;
    }

    /**
     * Retrieves the value of an attribute by its key, casting it to the specified type,
     * or returns a default value if the attribute is not present.
     *
     * <p>This method is useful for safely retrieving attribute values with type casting
     * and providing a default value when the attribute is not found. The casting is done
     * using the specified {@code Class} object.</p>
     *
     * @param key the key for the attribute.
     * @param clazz the {@code Class} object representing the type to cast the attribute value to.
     * @param defaultValue the value to return if the attribute is not present.
     * @param <A> the type of the attribute value.
     * @return the attribute value cast to the specified type, or the default value if not present.
     */
    public <A> A get(String key, Class<A> clazz, A defaultValue) {
        return clazz.cast(this.attributes.getOrDefault(key, defaultValue));
    }

    /**
     * Retrieves the value of an attribute by its key, casting it to the specified type.
     *
     * <p>This method is useful for retrieving attribute values with type casting. If the
     * attribute is not present, it returns {@code null}.</p>
     *
     * @param key the key for the attribute.
     * @param clazz the {@code Class} object representing the type to cast the attribute value to.
     * @param <A> the type of the attribute value.
     * @return the attribute value cast to the specified type, or {@code null} if not present.
     */
    public <A> A get(String key, Class<A> clazz) {
        return clazz.cast(this.attributes.get(key));
    }

    /**
     * Returns an unmodifiable view of the attributes map.
     *
     * <p>This method provides read-only access to the attributes stored in this {@code User} instance.
     * The returned map cannot be modified, ensuring that the attributes remain unchanged from outside.</p>
     *
     * @return an unmodifiable map containing all the attributes of the user.
     */
    public Map<String, Object> attributes() {
        return Collections.unmodifiableMap(attributes);
    }

}