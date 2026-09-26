package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map;

public final class KeyedInjector {

    private KeyedInjector() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Inject {
        String value() default "";
    }

    /** Sets each @Inject field of target's class from the registry. */
    public static void inject(Object target, Map<String, Object> registry) throws IllegalAccessException {
        throw new UnsupportedOperationException("TODO");
    }
}
