package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Validator {

    private Validator() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotEmpty {
        String message() default "Field must not be empty";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Range {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;

        String message() default "Value out of range";
    }

    public record Violation(String field, String message) {
        @Override
        public String toString() {
            return field + ": " + message;
        }
    }

    /** Every broken @NotEmpty or @Range rule on obj's declared fields, sorted by field name. */
    public static List<Violation> validate(Object obj) {
        List<Violation> violations = new ArrayList<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("cannot read " + field.getName(), e);
            }
            NotEmpty notEmpty = field.getAnnotation(NotEmpty.class);
            if (notEmpty != null && (value == null || (value instanceof String s && s.isBlank()))) {
                violations.add(new Violation(field.getName(), notEmpty.message()));
            }
            Range range = field.getAnnotation(Range.class);
            if (range != null && value instanceof Number n) {
                long v = n.longValue();
                if (v < range.min() || v > range.max()) {
                    violations.add(new Violation(field.getName(), range.message()));
                }
            }
            if (!violations.isEmpty()) {
                break;
            }
        }
        violations.sort(Comparator.comparing(Violation::field).thenComparing(Violation::message));
        return violations;
    }
}
