package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NullCheck {

    private NullCheck() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    public @interface NonNull {
    }

    /** "<field> is null" and "<field> has a null element" problems, sorted. */
    public static List<String> problems(Object bean) {
        List<String> problems = new ArrayList<>();
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = read(field, bean);
            AnnotatedType type = field.getAnnotatedType();
            if (type.isAnnotationPresent(NonNull.class) && value == null) {
                problems.add(field.getName() + " is null");
            }
            if (type instanceof AnnotatedParameterizedType p && value instanceof Collection<?> c) {
                AnnotatedType[] args = p.getAnnotatedActualTypeArguments();
                if (args.length == 1 && containsNull(c)) {
                    problems.add(field.getName() + " has a null element");
                }
            }
        }
        problems.sort(null);
        return problems;
    }

    private static boolean containsNull(Collection<?> c) {
        for (Object o : c) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    private static Object read(Field field, Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot read " + field.getName(), e);
        }
    }
}
