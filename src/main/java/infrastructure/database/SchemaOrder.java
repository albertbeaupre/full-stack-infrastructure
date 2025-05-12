package infrastructure.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to customize schema generation for individual fields.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaOrder {
    /**
     * Defines ordering of fields in the schema. Fields with lower values appear first.
     */
    int order();
}
