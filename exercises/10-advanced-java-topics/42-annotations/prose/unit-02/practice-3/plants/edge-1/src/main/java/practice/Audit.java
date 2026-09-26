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
        Class<?> found = null;
        for (Class<?> c = type.getSuperclass(); c != null; c = c.getSuperclass()) {
            if (c.getDeclaredAnnotation(Auditable.class) != null) {
                found = c;
            }
        }
        return found == null ? "none"
                : found.getDeclaredAnnotation(Auditable.class).level() + " from " + found.getSimpleName();
    }

    /** The @Author value as seen on type. */
    public static Optional<String> authorOf(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(Author.class)).map(Author::value);
    }
}
