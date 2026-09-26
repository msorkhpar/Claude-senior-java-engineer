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
        Auditable seen = type.getAnnotation(Auditable.class);
        if (seen == null) {
            return "none";
        }
        if (type.getDeclaredAnnotation(Auditable.class) != null) {
            return seen.level() + " declared";
        }
        Class<?> owner = type.getSuperclass();
        while (owner.getDeclaredAnnotation(Auditable.class) == null) {
            owner = owner.getSuperclass();
        }
        return seen.level() + " from " + owner.getSimpleName();
    }

    /** The @Author value as seen on type. */
    public static Optional<String> authorOf(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(Author.class)).map(Author::value);
    }
}
