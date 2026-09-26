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
        for (Field field : target.getClass().getDeclaredFields()) {
            Inject inject = field.getAnnotation(Inject.class);
            String key = inject == null || inject.value().isEmpty() ? field.getName() : inject.value();
            Object value = registry.get(key);
            if (value != null) {
                field.setAccessible(true);
                field.set(target, value);
            }
        }
    }
}
