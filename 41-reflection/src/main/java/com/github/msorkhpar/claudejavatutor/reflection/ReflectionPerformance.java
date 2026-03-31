package com.github.msorkhpar.claudejavatutor.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;

/**
 * Demonstrates reflection performance considerations and optimization techniques.
 * Covers benchmarking reflection vs direct access, caching strategies,
 * MethodHandle as an alternative, and best practices for minimizing overhead.
 */
public class ReflectionPerformance {

    // --- Sample class for benchmarking ---

    public static class BenchmarkTarget {
        private String value;

        public BenchmarkTarget() {
            this.value = "default";
        }

        public BenchmarkTarget(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String process(String input) {
            return input.toUpperCase();
        }

        private String secretProcess(String input) {
            return "SECRET:" + input;
        }
    }

    // --- Performance measurement record ---

    public record BenchmarkResult(String label, long durationNanos, int iterations) {
        public double nanosPerOp() {
            return (double) durationNanos / iterations;
        }
    }

    // --- Direct vs Reflection benchmarks ---

    /**
     * Measures the time to invoke a getter directly.
     */
    public static BenchmarkResult benchmarkDirectGetter(int iterations) {
        BenchmarkTarget target = new BenchmarkTarget("test");
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String ignored = target.getValue();
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("Direct getter", elapsed, iterations);
    }

    /**
     * Measures the time to invoke a getter via reflection (without caching).
     */
    public static BenchmarkResult benchmarkReflectionGetterUncached(int iterations)
            throws ReflectiveOperationException {
        BenchmarkTarget target = new BenchmarkTarget("test");
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Method method = BenchmarkTarget.class.getMethod("getValue");
            method.invoke(target);
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("Reflection getter (uncached)", elapsed, iterations);
    }

    /**
     * Measures the time to invoke a getter via cached reflection.
     */
    public static BenchmarkResult benchmarkReflectionGetterCached(int iterations)
            throws ReflectiveOperationException {
        BenchmarkTarget target = new BenchmarkTarget("test");
        Method method = BenchmarkTarget.class.getMethod("getValue");
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            method.invoke(target);
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("Reflection getter (cached)", elapsed, iterations);
    }

    /**
     * Measures the time to invoke a getter via MethodHandle.
     */
    public static BenchmarkResult benchmarkMethodHandle(int iterations) throws Throwable {
        BenchmarkTarget target = new BenchmarkTarget("test");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = lookup.findVirtual(BenchmarkTarget.class, "getValue",
                MethodType.methodType(String.class));
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String ignored = (String) handle.invoke(target);
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("MethodHandle getter", elapsed, iterations);
    }

    /**
     * Measures object creation via direct constructor call.
     */
    public static BenchmarkResult benchmarkDirectConstruction(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            BenchmarkTarget ignored = new BenchmarkTarget("value");
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("Direct construction", elapsed, iterations);
    }

    /**
     * Measures object creation via reflection.
     */
    public static BenchmarkResult benchmarkReflectionConstruction(int iterations)
            throws ReflectiveOperationException {
        Constructor<BenchmarkTarget> constructor =
                BenchmarkTarget.class.getConstructor(String.class);
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            BenchmarkTarget ignored = constructor.newInstance("value");
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("Reflection construction (cached)", elapsed, iterations);
    }

    // --- Caching strategies ---

    /**
     * Demonstrates a method cache for reflection.
     */
    public static class MethodCache {
        private final Map<String, Method> cache = new HashMap<>();

        public Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes)
                throws NoSuchMethodException {
            String key = clazz.getName() + "#" + name + Arrays.toString(paramTypes);
            Method method = cache.get(key);
            if (method == null) {
                method = clazz.getDeclaredMethod(name, paramTypes);
                method.setAccessible(true);
                cache.put(key, method);
            }
            return method;
        }

        public int cacheSize() {
            return cache.size();
        }

        public void clear() {
            cache.clear();
        }
    }

    /**
     * Demonstrates a field cache for reflection.
     */
    public static class FieldCache {
        private final Map<String, Field> cache = new HashMap<>();

        public Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
            String key = clazz.getName() + "#" + name;
            Field field = cache.get(key);
            if (field == null) {
                field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                cache.put(key, field);
            }
            return field;
        }

        public int cacheSize() {
            return cache.size();
        }
    }

    // --- MethodHandle examples ---

    /**
     * Creates a MethodHandle for a given method.
     */
    public static MethodHandle createMethodHandle(Class<?> clazz, String methodName,
                                                   MethodType methodType) throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        return lookup.findVirtual(clazz, methodName, methodType);
    }

    /**
     * Creates a MethodHandle for a static method.
     */
    public static MethodHandle createStaticMethodHandle(Class<?> clazz, String methodName,
                                                         MethodType methodType) throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        return lookup.findStatic(clazz, methodName, methodType);
    }

    /**
     * Creates a MethodHandle for field access (getter).
     */
    public static MethodHandle createFieldGetter(Class<?> clazz, String fieldName, Class<?> fieldType)
            throws NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        return lookup.findGetter(clazz, fieldName, fieldType);
    }

    /**
     * Creates a MethodHandle for field access (setter).
     */
    public static MethodHandle createFieldSetter(Class<?> clazz, String fieldName, Class<?> fieldType)
            throws NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        return lookup.findSetter(clazz, fieldName, fieldType);
    }

    // --- Reflection overhead analysis ---

    /**
     * Measures field read via direct access vs reflection vs MethodHandle.
     */
    public static Map<String, BenchmarkResult> compareFieldAccess(int iterations) throws Throwable {
        Map<String, BenchmarkResult> results = new LinkedHashMap<>();
        BenchmarkTarget target = new BenchmarkTarget("test");

        // Direct
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String ignored = target.getValue();
        }
        results.put("direct", new BenchmarkResult("Direct field read", System.nanoTime() - start, iterations));

        // Cached reflection
        Field field = BenchmarkTarget.class.getDeclaredField("value");
        field.setAccessible(true);
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String ignored = (String) field.get(target);
        }
        results.put("reflection", new BenchmarkResult("Reflection field read", System.nanoTime() - start, iterations));

        // MethodHandle
        MethodHandle getter = createFieldGetter(BenchmarkTarget.class, "value", String.class);
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String ignored = (String) getter.invoke(target);
        }
        results.put("methodHandle", new BenchmarkResult("MethodHandle field read", System.nanoTime() - start, iterations));

        return results;
    }

    /**
     * Demonstrates setAccessible overhead by comparing calls with and without setAccessible.
     */
    public static BenchmarkResult benchmarkSetAccessible(int iterations) throws ReflectiveOperationException {
        BenchmarkTarget target = new BenchmarkTarget("test");
        Method method = BenchmarkTarget.class.getDeclaredMethod("secretProcess", String.class);
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            method.setAccessible(true);
            method.invoke(target, "data");
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("setAccessible each call", elapsed, iterations);
    }

    /**
     * Demonstrates setAccessible called once (cached).
     */
    public static BenchmarkResult benchmarkSetAccessibleOnce(int iterations) throws ReflectiveOperationException {
        BenchmarkTarget target = new BenchmarkTarget("test");
        Method method = BenchmarkTarget.class.getDeclaredMethod("secretProcess", String.class);
        method.setAccessible(true);
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            method.invoke(target, "data");
        }
        long elapsed = System.nanoTime() - start;
        return new BenchmarkResult("setAccessible once", elapsed, iterations);
    }
}
