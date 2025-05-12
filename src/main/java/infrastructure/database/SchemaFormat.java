// SchemaFormat.java
package infrastructure.database;

import java.lang.annotation.*;

/**
 * Specify a custom format (e.g. date/time pattern or epoch unit).
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaFormat {
    /**
     * Format string (e.g. "yyyy-MM-dd" or "seconds-since-epoch").
     */
    String value();
}
