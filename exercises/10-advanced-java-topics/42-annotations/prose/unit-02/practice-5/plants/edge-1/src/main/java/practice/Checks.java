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
        List<String> failures = new ArrayList<>();
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = read(field, bean);
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                List<Annotation> constraints = constraintsOf(annotation);
                for (Annotation constraint : constraints) {
                    Constraint c = constraint.annotationType().getAnnotation(Constraint.class);
                    if (!create(c.validatedBy()).ok(value)) {
                        failures.add(field.getName() + ": " + constraint.annotationType().getSimpleName());
                    }
                }
                if (!constraints.isEmpty()) {
                    break;
                }
            }
        }
        failures.sort(null);
        return failures;
    }

    private static List<Annotation> constraintsOf(Annotation annotation) {
        if (annotation.annotationType().isAnnotationPresent(Constraint.class)) {
            return List.of(annotation);
        }
        List<Annotation> composed = new ArrayList<>();
        for (Annotation meta : annotation.annotationType().getDeclaredAnnotations()) {
            if (meta.annotationType().isAnnotationPresent(Constraint.class)) {
                composed.add(meta);
            }
        }
        return composed;
    }

    private static Object read(Field field, Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot read " + field.getName(), e);
        }
    }

    private static Check create(Class<? extends Check> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + type.getName(), e);
        }
    }
}
