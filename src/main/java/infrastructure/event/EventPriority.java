package infrastructure.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code EventPriority} annotation interface is used on methods within an {@code EventListener}
 * to determine their priority to be handled at when an {@code Event} is published. Lower priority
 * levels mean that the {@code Event} will be handled later. Higher priority means it will be handled
 * sooner.
 *
 * @author Albert Beaupre
 * @since August 29th, 2024
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventPriority {

    /**
     * The priority value (default of 0) used to determine what priority level
     * the method within an {@code EventListener} is going to be handled at.
     *
     * @return the priority level
     */
    int priority() default 0;

}
