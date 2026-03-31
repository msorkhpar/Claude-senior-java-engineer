package com.github.msorkhpar.claudejavatutor.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Reflection Performance Considerations Tests")
class ReflectionPerformanceTest {

    private static final int ITERATIONS = 10_000;

    @Nested
    @DisplayName("Direct vs Reflection Benchmarks")
    class BenchmarkTests {

        @Test
        @DisplayName("Direct getter benchmark should complete successfully")
        void testDirectGetterBenchmark() {
            var result = ReflectionPerformance.benchmarkDirectGetter(ITERATIONS);

            assertThat(result.label()).isEqualTo("Direct getter");
            assertThat(result.iterations()).isEqualTo(ITERATIONS);
            assertThat(result.durationNanos()).isGreaterThanOrEqualTo(0);
            assertThat(result.nanosPerOp()).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Reflection uncached getter benchmark should complete")
        void testReflectionUncachedBenchmark() throws Exception {
            var result = ReflectionPerformance.benchmarkReflectionGetterUncached(ITERATIONS);

            assertThat(result.label()).isEqualTo("Reflection getter (uncached)");
            assertThat(result.iterations()).isEqualTo(ITERATIONS);
            assertThat(result.durationNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Reflection cached getter benchmark should complete")
        void testReflectionCachedBenchmark() throws Exception {
            var result = ReflectionPerformance.benchmarkReflectionGetterCached(ITERATIONS);

            assertThat(result.label()).isEqualTo("Reflection getter (cached)");
            assertThat(result.durationNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("MethodHandle getter benchmark should complete")
        void testMethodHandleBenchmark() throws Throwable {
            var result = ReflectionPerformance.benchmarkMethodHandle(ITERATIONS);

            assertThat(result.label()).isEqualTo("MethodHandle getter");
            assertThat(result.durationNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Cached reflection should be faster than uncached reflection")
        void testCachedVsUncachedReflection() throws Exception {
            int warmUpIterations = 50_000;
            var uncached = ReflectionPerformance.benchmarkReflectionGetterUncached(warmUpIterations);
            var cached = ReflectionPerformance.benchmarkReflectionGetterCached(warmUpIterations);

            // Cached should generally be faster due to no repeated method lookup
            assertThat(cached.nanosPerOp()).isLessThan(uncached.nanosPerOp());
        }

        @Test
        @DisplayName("Direct construction benchmark should complete")
        void testDirectConstructionBenchmark() {
            var result = ReflectionPerformance.benchmarkDirectConstruction(ITERATIONS);

            assertThat(result.label()).isEqualTo("Direct construction");
            assertThat(result.durationNanos()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Reflection construction benchmark should complete")
        void testReflectionConstructionBenchmark() throws Exception {
            var result = ReflectionPerformance.benchmarkReflectionConstruction(ITERATIONS);

            assertThat(result.label()).isEqualTo("Reflection construction (cached)");
            assertThat(result.durationNanos()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Method Cache")
    class MethodCacheTests {

        @Test
        @DisplayName("Should cache and retrieve methods")
        void testMethodCaching() throws Exception {
            var cache = new ReflectionPerformance.MethodCache();

            var method1 = cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "getValue");
            var method2 = cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "getValue");

            assertThat(method1).isSameAs(method2);
            assertThat(cache.cacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should cache multiple different methods")
        void testMultipleMethodCaching() throws Exception {
            var cache = new ReflectionPerformance.MethodCache();

            cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "getValue");
            cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "setValue", String.class);
            cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "process", String.class);

            assertThat(cache.cacheSize()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should invoke cached method correctly")
        void testInvokeCachedMethod() throws Exception {
            var cache = new ReflectionPerformance.MethodCache();
            var target = new ReflectionPerformance.BenchmarkTarget("hello");

            var method = cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "process", String.class);
            Object result = method.invoke(target, "world");

            assertThat(result).isEqualTo("WORLD");
        }

        @Test
        @DisplayName("Should throw NoSuchMethodException for non-existent method")
        void testCacheNonExistentMethod() {
            var cache = new ReflectionPerformance.MethodCache();

            assertThatThrownBy(() -> cache.getMethod(
                    ReflectionPerformance.BenchmarkTarget.class, "nonExistent"))
                    .isInstanceOf(NoSuchMethodException.class);
        }

        @Test
        @DisplayName("Should clear cache")
        void testClearCache() throws Exception {
            var cache = new ReflectionPerformance.MethodCache();
            cache.getMethod(ReflectionPerformance.BenchmarkTarget.class, "getValue");
            assertThat(cache.cacheSize()).isEqualTo(1);

            cache.clear();
            assertThat(cache.cacheSize()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Field Cache")
    class FieldCacheTests {

        @Test
        @DisplayName("Should cache and retrieve fields")
        void testFieldCaching() throws Exception {
            var cache = new ReflectionPerformance.FieldCache();

            var field1 = cache.getField(ReflectionPerformance.BenchmarkTarget.class, "value");
            var field2 = cache.getField(ReflectionPerformance.BenchmarkTarget.class, "value");

            assertThat(field1).isSameAs(field2);
            assertThat(cache.cacheSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should read field value through cache")
        void testReadThroughCache() throws Exception {
            var cache = new ReflectionPerformance.FieldCache();
            var target = new ReflectionPerformance.BenchmarkTarget("cached_value");

            var field = cache.getField(ReflectionPerformance.BenchmarkTarget.class, "value");
            Object result = field.get(target);

            assertThat(result).isEqualTo("cached_value");
        }
    }

    @Nested
    @DisplayName("MethodHandle Operations")
    class MethodHandleTests {

        @Test
        @DisplayName("Should create and invoke MethodHandle for getter")
        void testCreateMethodHandle() throws Throwable {
            MethodHandle handle = ReflectionPerformance.createMethodHandle(
                    ReflectionPerformance.BenchmarkTarget.class,
                    "getValue",
                    MethodType.methodType(String.class));

            var target = new ReflectionPerformance.BenchmarkTarget("test");
            String result = (String) handle.invoke(target);

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Should create and invoke MethodHandle for method with arguments")
        void testMethodHandleWithArgs() throws Throwable {
            MethodHandle handle = ReflectionPerformance.createMethodHandle(
                    ReflectionPerformance.BenchmarkTarget.class,
                    "process",
                    MethodType.methodType(String.class, String.class));

            var target = new ReflectionPerformance.BenchmarkTarget();
            String result = (String) handle.invoke(target, "hello");

            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should create field getter MethodHandle")
        void testFieldGetterMethodHandle() throws Throwable {
            MethodHandle getter = ReflectionPerformance.createFieldGetter(
                    ReflectionPerformance.BenchmarkTarget.class, "value", String.class);

            var target = new ReflectionPerformance.BenchmarkTarget("field_value");
            String result = (String) getter.invoke(target);

            assertThat(result).isEqualTo("field_value");
        }

        @Test
        @DisplayName("Should create field setter MethodHandle")
        void testFieldSetterMethodHandle() throws Throwable {
            MethodHandle setter = ReflectionPerformance.createFieldSetter(
                    ReflectionPerformance.BenchmarkTarget.class, "value", String.class);
            MethodHandle getter = ReflectionPerformance.createFieldGetter(
                    ReflectionPerformance.BenchmarkTarget.class, "value", String.class);

            var target = new ReflectionPerformance.BenchmarkTarget("old");
            setter.invoke(target, "new_value");
            String result = (String) getter.invoke(target);

            assertThat(result).isEqualTo("new_value");
        }

        @Test
        @DisplayName("Should throw NoSuchMethodException for invalid MethodHandle")
        void testInvalidMethodHandle() {
            assertThatThrownBy(() -> ReflectionPerformance.createMethodHandle(
                    ReflectionPerformance.BenchmarkTarget.class,
                    "nonExistent",
                    MethodType.methodType(void.class)))
                    .isInstanceOf(NoSuchMethodException.class);
        }
    }

    @Nested
    @DisplayName("Comparative Analysis")
    class ComparativeTests {

        @Test
        @DisplayName("Should compare field access approaches and return results")
        void testCompareFieldAccess() throws Throwable {
            Map<String, ReflectionPerformance.BenchmarkResult> results =
                    ReflectionPerformance.compareFieldAccess(ITERATIONS);

            assertThat(results).containsKeys("direct", "reflection", "methodHandle");
            assertThat(results.get("direct").durationNanos()).isGreaterThanOrEqualTo(0);
            assertThat(results.get("reflection").durationNanos()).isGreaterThan(0);
            assertThat(results.get("methodHandle").durationNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("BenchmarkResult record should calculate nanosPerOp correctly")
        void testBenchmarkResultCalculation() {
            var result = new ReflectionPerformance.BenchmarkResult("test", 1000, 100);

            assertThat(result.nanosPerOp()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("BenchmarkResult with zero iterations should handle division")
        void testBenchmarkResultZeroIterations() {
            var result = new ReflectionPerformance.BenchmarkResult("test", 1000, 0);

            assertThat(result.nanosPerOp()).isInfinite();
        }
    }

    @Nested
    @DisplayName("setAccessible Performance")
    class SetAccessibleTests {

        @Test
        @DisplayName("setAccessible called each time benchmark should complete")
        void testSetAccessibleEachCall() throws Exception {
            var result = ReflectionPerformance.benchmarkSetAccessible(ITERATIONS);

            assertThat(result.label()).isEqualTo("setAccessible each call");
            assertThat(result.durationNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("setAccessible called once benchmark should complete")
        void testSetAccessibleOnce() throws Exception {
            var result = ReflectionPerformance.benchmarkSetAccessibleOnce(ITERATIONS);

            assertThat(result.label()).isEqualTo("setAccessible once");
            assertThat(result.durationNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Calling setAccessible once should be faster than calling each time")
        void testSetAccessibleOnceVsEachTime() throws Exception {
            int warmIterations = 50_000;
            var eachTime = ReflectionPerformance.benchmarkSetAccessible(warmIterations);
            var once = ReflectionPerformance.benchmarkSetAccessibleOnce(warmIterations);

            assertThat(once.nanosPerOp()).isLessThanOrEqualTo(eachTime.nanosPerOp());
        }
    }
}
