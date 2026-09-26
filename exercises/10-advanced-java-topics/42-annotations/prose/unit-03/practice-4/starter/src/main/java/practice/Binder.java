package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

public final class Binder {

    private Binder() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface RequestParam {
        String name();

        boolean required() default true;
    }

    /** The arguments for m, bound from the query by each parameter's @RequestParam. */
    public static Object[] bind(Method m, Map<String, String> query) {
        throw new UnsupportedOperationException("TODO");
    }
}
