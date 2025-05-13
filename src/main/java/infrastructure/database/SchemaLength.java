package infrastructure.database;

import java.lang.annotation.*;

/**
 * Constrain min/max length for Strings, collections, and arrays.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaLength {

    /**
     * Maximum allowed length (inclusive).
     */
    int max() default 120;
}
