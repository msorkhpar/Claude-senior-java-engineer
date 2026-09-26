package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Optional;

public final class Tx {

    private Tx() {
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Transactional {
        boolean readOnly() default false;
    }

    /** "read-only" or "read-write" for type.getMethod(method), from the method or else the class. */
    public static Optional<String> mode(Class<?> type, String method) {
        Method m;
        try {
            m = type.getMethod(method);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("no public method " + method, e);
        }
        Transactional tx = m.getAnnotation(Transactional.class);
        if (tx == null) {
            tx = type.getAnnotation(Transactional.class);
        }
        return Optional.ofNullable(tx).map(t -> t.readOnly() ? "read-only" : "read-write");
    }
}
