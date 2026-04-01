package com.github.msorkhpar.claudejavatutor.annotations;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Demonstrates the three annotation retention policies: SOURCE, CLASS, and RUNTIME.
 * Shows how each retention policy affects annotation availability at different stages.
 */
public class RetentionPolicies {

    // ========================
    // SOURCE retention — discarded by the compiler, not in bytecode
    // ========================

    /**
     * SOURCE retention annotation — only available during compilation.
     * Similar to @Override or @SuppressWarnings.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    public @interface CompileTimeOnly {
        String value() default "";
    }

    // ========================
    // CLASS retention — in bytecode but not available via reflection at runtime
    // ========================

    /**
     * CLASS retention annotation — available in .class file but NOT via reflection.
     * This is the default retention if none is specified.
     */
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface ClassLevelMeta {
        String description() default "";
    }

    // ========================
    // RUNTIME retention — available via reflection at runtime
    // ========================

    /**
     * RUNTIME retention annotation — fully available at runtime via reflection.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
    public @interface RuntimeInfo {
        String description() default "";

        String author() default "unknown";

        int priority() default 0;
    }

    // ========================
    // Target element types demonstration
    // ========================

    /**
     * Annotation targeting TYPE_USE — can be used on any type reference (Java 8+).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    public @interface NonNull {
    }

    /**
     * Annotation targeting TYPE_PARAMETER — for generic type parameters.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_PARAMETER)
    public @interface Covariant {
    }

    /**
     * Annotation targeting PARAMETER — method parameters.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface Validated {
        String message() default "Invalid parameter";
    }

    /**
     * Annotation targeting LOCAL_VARIABLE — not available at runtime via reflection.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.LOCAL_VARIABLE)
    public @interface Immutable {
    }

    // ========================
    // Example classes
    // ========================

    @RuntimeInfo(description = "Service demonstrating retention policies", author = "Tutorial", priority = 1)
    public static class DemoService {

        @RuntimeInfo(description = "Primary field", priority = 2)
        private String name;

        @CompileTimeOnly("This won't be available at runtime")
        public void sourceRetained() {
            // @CompileTimeOnly is not available via reflection
        }

        @ClassLevelMeta(description = "In bytecode but not in reflection")
        public void classRetained() {
            // @ClassLevelMeta is not available via reflection
        }

        @RuntimeInfo(description = "Fully available at runtime", author = "Demo", priority = 5)
        public void runtimeRetained() {
            // @RuntimeInfo IS available via reflection
        }

        public String processInput(@Validated(message = "Name required") String name) {
            return "Processed: " + name;
        }
    }

    // ========================
    // Utility methods for querying annotations
    // ========================

    /**
     * Checks if a class has a RUNTIME annotation of the given type.
     */
    public static <A extends Annotation> boolean hasAnnotation(Class<?> clazz, Class<A> annotationType) {
        return clazz.isAnnotationPresent(annotationType);
    }

    /**
     * Gets a RUNTIME annotation from a class if present.
     */
    public static <A extends Annotation> Optional<A> getAnnotation(Class<?> clazz, Class<A> annotationType) {
        return Optional.ofNullable(clazz.getAnnotation(annotationType));
    }

    /**
     * Gets a RUNTIME annotation from a method if present.
     */
    public static <A extends Annotation> Optional<A> getMethodAnnotation(
            Class<?> clazz, String methodName, Class<A> annotationType) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            return Optional.ofNullable(method.getAnnotation(annotationType));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns method names that have the specified RUNTIME annotation.
     */
    public static List<String> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotationType))
                .map(Method::getName)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a SOURCE or CLASS annotation is present — demonstrates it is NOT available.
     */
    public static boolean isSourceAnnotationPresent(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            return method.isAnnotationPresent(CompileTimeOnly.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Checks if a CLASS-retained annotation is present via reflection — demonstrates it is NOT.
     */
    public static boolean isClassAnnotationPresent(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            return method.isAnnotationPresent(ClassLevelMeta.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Gets all annotations on a class (only RUNTIME ones are visible).
     */
    public static Annotation[] getAllAnnotations(Class<?> clazz) {
        return clazz.getAnnotations();
    }

    /**
     * Gets all annotations declared directly on a class (not inherited).
     */
    public static Annotation[] getDeclaredAnnotations(Class<?> clazz) {
        return clazz.getDeclaredAnnotations();
    }

    /**
     * Demonstrates parameter annotations — reads @Validated from method parameters.
     */
    public static Optional<Validated> getParameterValidation(Class<?> clazz, String methodName, int paramIndex) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Annotation[][] paramAnnotations = method.getParameterAnnotations();
                    if (paramIndex < paramAnnotations.length) {
                        for (Annotation ann : paramAnnotations[paramIndex]) {
                            if (ann instanceof Validated validated) {
                                return Optional.of(validated);
                            }
                        }
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
