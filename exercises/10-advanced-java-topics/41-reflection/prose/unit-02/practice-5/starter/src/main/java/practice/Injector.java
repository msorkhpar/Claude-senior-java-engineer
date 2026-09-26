package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

public final class Injector {

    /** Marks a field to inject. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Inject {
    }

    private Injector() {
    }

    /** Sets every @Inject field of target, inherited ones too, to the bean of its type. */
    public static void inject(Object target, Map<Class<?>, Object> beans) {
        throw new UnsupportedOperationException("TODO");
    }
}
