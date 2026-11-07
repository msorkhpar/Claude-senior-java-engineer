package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

/**
 * Tells the tracing agent how closely to trace the instances of a handler class.
 *
 * A handler is traced at the level its {@code @Traced} says. The annotation may sit on the
 * class, on any of its superclasses, or on any interface the class or a superclass implements,
 * directly or through another interface. One on the class or a superclass wins over one on an
 * interface. A class with none anywhere is not traced.
 */
public class TraceLookup {

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Traced {
        String level() default "SPANS";
    }

    /** The level {@code handler} is traced at, or empty when it isn't traced. */
    public Optional<String> levelOf(Class<?> handler) {
        Traced onClasses = handler.getAnnotation(Traced.class);
        if (onClasses != null) return Optional.of(onClasses.level());
        return fromInterfaces(handler.getInterfaces());
    }

    /** The level on the first of {@code interfaces}, or on an interface they extend, that is traced. */
    Optional<String> fromInterfaces(Class<?>[] interfaces) {
        for (Class<?> i : interfaces) {
            Traced t = i.getAnnotation(Traced.class);
            if (t != null) return Optional.of(t.level());
        }
        return Optional.empty();
    }
}
