package com.github.msorkhpar.claudejavatutor.annotations;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates annotation processing and runtime code generation patterns in Java.
 * Covers reflection-based processing, proxy-based AOP, and annotation-driven frameworks.
 */
public class AnnotationProcessing {

    // ========================
    // Annotations for a mini-framework
    // ========================

    /**
     * Marks a method to be invoked automatically on initialization.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PostConstruct {
        int order() default 0;
    }

    /**
     * Marks a method to be invoked before the object is destroyed.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PreDestroy {
        int order() default 0;
    }

    /**
     * Marks a field for automatic injection by name.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Inject {
        String value() default "";
    }

    /**
     * Marks a method for logging/timing (AOP-style).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Timed {
        String label() default "";
    }

    /**
     * Marks a method as cacheable — results are cached by arguments.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Cacheable {
        String cacheName() default "default";
    }

    /**
     * Marks a method as requiring a specific permission.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RequiresPermission {
        String value();
    }

    // ========================
    // Lifecycle processor
    // ========================

    /**
     * Processes @PostConstruct and @PreDestroy annotations on an object.
     */
    public static class LifecycleProcessor {

        /**
         * Invokes all @PostConstruct methods in order.
         */
        public static void initialize(Object target) throws Exception {
            List<Method> methods = getAnnotatedMethods(target.getClass(), PostConstruct.class);
            methods.sort(Comparator.comparingInt(m -> m.getAnnotation(PostConstruct.class).order()));
            for (Method method : methods) {
                method.setAccessible(true);
                method.invoke(target);
            }
        }

        /**
         * Invokes all @PreDestroy methods in order.
         */
        public static void destroy(Object target) throws Exception {
            List<Method> methods = getAnnotatedMethods(target.getClass(), PreDestroy.class);
            methods.sort(Comparator.comparingInt(m -> m.getAnnotation(PreDestroy.class).order()));
            for (Method method : methods) {
                method.setAccessible(true);
                method.invoke(target);
            }
        }

        private static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
            return Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(annotation))
                    .collect(Collectors.toList());
        }
    }

    // ========================
    // Field injection processor
    // ========================

    /**
     * Simple dependency injector that resolves @Inject fields from a registry map.
     */
    public static class FieldInjector {

        /**
         * Injects values from the registry into @Inject-annotated fields.
         * Field name or @Inject(value) is used as the lookup key.
         */
        public static void inject(Object target, Map<String, Object> registry) throws Exception {
            for (Field field : target.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Inject annotation = field.getAnnotation(Inject.class);
                    String key = annotation.value().isEmpty() ? field.getName() : annotation.value();
                    Object value = registry.get(key);
                    if (value != null) {
                        field.setAccessible(true);
                        field.set(target, value);
                    }
                }
            }
        }
    }

    // ========================
    // Annotation-driven proxy (AOP simulation)
    // ========================

    /**
     * Invocation log entry.
     */
    public record InvocationLog(String method, long durationNanos, Object result) {
    }

    /**
     * Creates a dynamic proxy that intercepts @Timed methods.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createTimedProxy(T target, Class<T> iface, List<InvocationLog> logs) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> {
                    // Find the actual method on the target class to check for annotation
                    Method targetMethod;
                    try {
                        targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        return method.invoke(target, args);
                    }

                    if (targetMethod.isAnnotationPresent(Timed.class)) {
                        long start = System.nanoTime();
                        Object result = method.invoke(target, args);
                        long duration = System.nanoTime() - start;
                        String label = targetMethod.getAnnotation(Timed.class).label();
                        String logLabel = label.isEmpty() ? method.getName() : label;
                        logs.add(new InvocationLog(logLabel, duration, result));
                        return result;
                    }
                    return method.invoke(target, args);
                }
        );
    }

    /**
     * Creates a dynamic proxy that checks @RequiresPermission before invoking.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createSecuredProxy(T target, Class<T> iface, Set<String> userPermissions) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                (proxy, method, args) -> {
                    Method targetMethod;
                    try {
                        targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        return method.invoke(target, args);
                    }

                    if (targetMethod.isAnnotationPresent(RequiresPermission.class)) {
                        String required = targetMethod.getAnnotation(RequiresPermission.class).value();
                        if (!userPermissions.contains(required)) {
                            throw new SecurityException("Missing permission: " + required);
                        }
                    }
                    return method.invoke(target, args);
                }
        );
    }

    // ========================
    // Annotation metadata scanner
    // ========================

    /**
     * Result of scanning a class for annotation metadata.
     */
    public record AnnotationScanResult(
            String className,
            List<String> classAnnotations,
            Map<String, List<String>> methodAnnotations,
            Map<String, List<String>> fieldAnnotations
    ) {
    }

    /**
     * Scans a class and collects all RUNTIME annotations on the class, methods, and fields.
     */
    public static AnnotationScanResult scanAnnotations(Class<?> clazz) {
        String className = clazz.getSimpleName();

        List<String> classAnnotations = Arrays.stream(clazz.getDeclaredAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .collect(Collectors.toList());

        Map<String, List<String>> methodAnnotations = new LinkedHashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            if (annotations.length > 0) {
                methodAnnotations.put(method.getName(),
                        Arrays.stream(annotations)
                                .map(a -> a.annotationType().getSimpleName())
                                .collect(Collectors.toList()));
            }
        }

        Map<String, List<String>> fieldAnnotations = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            if (annotations.length > 0) {
                fieldAnnotations.put(field.getName(),
                        Arrays.stream(annotations)
                                .map(a -> a.annotationType().getSimpleName())
                                .collect(Collectors.toList()));
            }
        }

        return new AnnotationScanResult(className, classAnnotations, methodAnnotations, fieldAnnotations);
    }

    // ========================
    // Example service classes for testing
    // ========================

    public static class SampleService {
        private final List<String> initLog = new ArrayList<>();

        @PostConstruct(order = 1)
        public void init() {
            initLog.add("init");
        }

        @PostConstruct(order = 2)
        public void warmUpCache() {
            initLog.add("warmUpCache");
        }

        @PreDestroy(order = 1)
        public void flushBuffers() {
            initLog.add("flushBuffers");
        }

        @PreDestroy(order = 2)
        public void closeConnections() {
            initLog.add("closeConnections");
        }

        public List<String> getInitLog() {
            return Collections.unmodifiableList(initLog);
        }
    }

    public static class InjectableService {
        @Inject("dataSource")
        private String dataSource;

        @Inject
        private String config;

        public String getDataSource() {
            return dataSource;
        }

        public String getConfig() {
            return config;
        }
    }

    /**
     * Interface for proxy demonstrations.
     */
    public interface Calculator {
        int add(int a, int b);

        int multiply(int a, int b);

        void adminReset();
    }

    /**
     * Implementation with @Timed and @RequiresPermission annotations.
     */
    public static class CalculatorImpl implements Calculator {

        @Timed(label = "addition")
        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Timed
        @Override
        public int multiply(int a, int b) {
            return a * b;
        }

        @RequiresPermission("ADMIN")
        @Override
        public void adminReset() {
            // reset logic
        }
    }
}
