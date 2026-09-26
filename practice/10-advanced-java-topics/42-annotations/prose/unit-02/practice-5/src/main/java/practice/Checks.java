package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class Checks {

    private Checks() {
    }

    public interface Check {
        boolean ok(Object value);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    public @interface Constraint {
        Class<? extends Check> validatedBy();
    }

    public static final class NotBlankCheck implements Check {
        @Override
        public boolean ok(Object value) {
            return value instanceof String s && !s.isBlank();
        }
    }

    public static final class PositiveCheck implements Check {
        @Override
        public boolean ok(Object value) {
            return value instanceof Number n && n.longValue() > 0;
        }
    }

    @Constraint(validatedBy = NotBlankCheck.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    public @interface NotBlank {
    }

    @Constraint(validatedBy = PositiveCheck.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    public @interface Positive {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Label {
        String value();
    }

    /** "<field>: <ConstraintSimpleName>" for every failed constraint, sorted. */
    public static List<String> failures(Object bean) {
        throw new UnsupportedOperationException("TODO");
    }
}
