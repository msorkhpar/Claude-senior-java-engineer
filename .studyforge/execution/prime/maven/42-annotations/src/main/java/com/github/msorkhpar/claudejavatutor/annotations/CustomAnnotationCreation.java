package com.github.msorkhpar.claudejavatutor.annotations;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates custom annotation creation in Java, including marker annotations,
 * single-value annotations, multi-value annotations, and repeatable annotations.
 */
public class CustomAnnotationCreation {

    // ========================
    // Marker Annotation (no elements)
    // ========================

    /**
     * A marker annotation indicating a class is thread-safe.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ThreadSafe {
    }

    // ========================
    // Single-Value Annotation
    // ========================

    /**
     * Annotation with a single 'value' element (allows shorthand usage).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Author {
        String value();
    }

    // ========================
    // Multi-Value Annotation
    // ========================

    /**
     * Annotation with multiple elements and default values.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ApiEndpoint {
        String path();

        String method() default "GET";

        String description() default "";

        int version() default 1;

        String[] produces() default {"application/json"};
    }

    // ========================
    // Repeatable Annotation
    // ========================

    /**
     * Container for repeatable @Role annotations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Roles {
        Role[] value();
    }

    /**
     * Repeatable annotation for specifying multiple roles.
     */
    @Repeatable(Roles.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Role {
        String value();
    }

    // ========================
    // Field-level Annotation
    // ========================

    /**
     * Annotation for field validation constraints.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotEmpty {
        String message() default "Field must not be empty";
    }

    /**
     * Annotation for numeric range validation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Range {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;

        String message() default "Value out of range";
    }

    // ========================
    // Inherited Annotation
    // ========================

    /**
     * Inherited annotation — subclasses automatically inherit it from parent.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Auditable {
        String level() default "INFO";
    }

    // ========================
    // Documented Annotation
    // ========================

    /**
     * Documented annotation — appears in Javadoc.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface ApiVersion {
        int major();

        int minor() default 0;
    }

    // ========================
    // Example classes using custom annotations
    // ========================

    @ThreadSafe
    @Author("Jane Doe")
    @Auditable(level = "DEBUG")
    @ApiVersion(major = 2, minor = 1)
    public static class UserService {

        @NotEmpty(message = "Name is required")
        private String name;

        @Range(min = 0, max = 150, message = "Age must be between 0 and 150")
        private int age;

        public UserService(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @ApiEndpoint(path = "/users", method = "GET", description = "List all users", version = 2)
        @Role("ADMIN")
        @Role("MANAGER")
        public String listUsers() {
            return "user list";
        }

        @ApiEndpoint(path = "/users", method = "POST", produces = {"application/json", "application/xml"})
        @Role("ADMIN")
        public String createUser() {
            return "user created";
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    /**
     * Subclass that inherits @Auditable from parent.
     */
    public static class AdminService extends UserService {
        public AdminService(String name, int age) {
            super(name, age);
        }
    }

    // ========================
    // Simple annotation processor (reflection-based)
    // ========================

    /**
     * Validation result for field validation.
     */
    public record ValidationError(String fieldName, String message) {
    }

    /**
     * Simple validator that processes @NotEmpty and @Range annotations at runtime.
     */
    public static List<ValidationError> validate(Object obj) {
        List<ValidationError> errors = new ArrayList<>();
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                // Check @NotEmpty
                if (field.isAnnotationPresent(NotEmpty.class)) {
                    NotEmpty notEmpty = field.getAnnotation(NotEmpty.class);
                    Object value = field.get(obj);
                    if (value == null || (value instanceof String s && s.isBlank())) {
                        errors.add(new ValidationError(field.getName(), notEmpty.message()));
                    }
                }

                // Check @Range
                if (field.isAnnotationPresent(Range.class)) {
                    Range range = field.getAnnotation(Range.class);
                    Object value = field.get(obj);
                    if (value instanceof Number number) {
                        int intVal = number.intValue();
                        if (intVal < range.min() || intVal > range.max()) {
                            errors.add(new ValidationError(field.getName(), range.message()));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + field.getName(), e);
            }
        }
        return errors;
    }

    /**
     * Reads @ApiEndpoint metadata from a method.
     */
    public static ApiEndpoint getEndpointInfo(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            return method.getAnnotation(ApiEndpoint.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Reads all @Role annotations from a method (repeatable).
     */
    public static Role[] getRoles(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            return method.getAnnotationsByType(Role.class);
        } catch (NoSuchMethodException e) {
            return new Role[0];
        }
    }
}
