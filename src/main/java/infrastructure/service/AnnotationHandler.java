package infrastructure.service;

import java.lang.annotation.Annotation;

/**
 * Your handler interface.
 * • return true to let subsequent handlers run;
 * • return false to break the chain for this annotation+owner.
 * • priority: lower values run first.
 */
public interface AnnotationHandler<A extends Annotation> {
    /**
     * Which annotation type you handle
     */
    Class<A> annotationType();

    /**
     * Process one instance; return false to stop propagation
     */
    boolean handle(A annotation, Object owner, AnnotationProcessor.ProcessingContext ctx);

    /**
     * Lower values = higher priority. Default 0.
     */
    default int priority() {
        return 0;
    }
}