package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

public final class Audit {

    private Audit() {
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Auditable {
        String level() default "INFO";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Author {
        String value();
    }

    /** "<level> declared", "<level> from <SimpleName>" or "none". */
    public static String describe(Class<?> type) {
        Auditable own = type.getDeclaredAnnotation(Auditable.class);
        if (own != null) {
            return own.level() + " declared";
        }
        java.util.ArrayDeque<Class<?>> todo = new java.util.ArrayDeque<>();
        if (type.getSuperclass() != null) {
            todo.add(type.getSuperclass());
        }
        todo.addAll(java.util.List.of(type.getInterfaces()));
        while (!todo.isEmpty()) {
            Class<?> c = todo.poll();
            Auditable a = c.getDeclaredAnnotation(Auditable.class);
            if (a != null) {
                return a.level() + " from " + c.getSimpleName();
            }
            if (c.getSuperclass() != null) {
                todo.add(c.getSuperclass());
            }
            todo.addAll(java.util.List.of(c.getInterfaces()));
        }
        return "none";
    }

    /** The @Author value as seen on type. */
    public static Optional<String> authorOf(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(Author.class)).map(Author::value);
    }
}
