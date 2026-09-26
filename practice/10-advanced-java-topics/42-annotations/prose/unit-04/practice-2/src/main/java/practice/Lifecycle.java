package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

public final class Lifecycle {

    private Lifecycle() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PostConstruct {
        int order() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PreDestroy {
        int order() default 0;
    }

    /** Calls target's @PostConstruct methods, lowest order first. */
    public static void initialize(Object target) throws Exception {
        throw new UnsupportedOperationException("TODO");
    }

    /** Calls target's @PreDestroy methods, lowest order first. */
    public static void destroy(Object target) throws Exception {
        throw new UnsupportedOperationException("TODO");
    }
}
