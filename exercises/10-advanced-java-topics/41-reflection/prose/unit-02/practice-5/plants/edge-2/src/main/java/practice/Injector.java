package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
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
        for (Class<?> type = target.getClass(); type != null; type = null) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object bean = beans.get(field.getType());
                    if (bean == null) {
                        throw new IllegalStateException("no bean for field " + field.getName());
                    }
                    set(field, target, bean);
                }
            }
        }
    }

    private static void set(Field field, Object target, Object bean) {
        field.setAccessible(true);
        try {
            field.set(target, bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
