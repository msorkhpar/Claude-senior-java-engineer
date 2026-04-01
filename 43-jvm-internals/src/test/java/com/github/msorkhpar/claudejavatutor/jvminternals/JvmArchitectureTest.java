package com.github.msorkhpar.claudejavatutor.jvminternals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JVM Architecture Tests")
class JvmArchitectureTest {

    @Nested
    @DisplayName("Class Loader Explorer")
    class ClassLoaderExplorerTest {

        private final JvmArchitecture.ClassLoaderExplorer explorer = new JvmArchitecture.ClassLoaderExplorer();

        @Test
        @DisplayName("Should identify bootstrap class loader for core classes")
        void testBootstrapClassLoaderForCoreClasses() {
            String loaderName = explorer.getClassLoaderName(String.class);
            assertThat(loaderName).isEqualTo("Bootstrap (null)");
        }

        @Test
        @DisplayName("Should identify non-bootstrap class loader for application classes")
        void testApplicationClassLoader() {
            String loaderName = explorer.getClassLoaderName(JvmArchitecture.class);
            assertThat(loaderName).isNotEqualTo("Bootstrap (null)");
        }

        @Test
        @DisplayName("Should return class loader hierarchy ending with Bootstrap")
        void testClassLoaderHierarchyEndsWithBootstrap() {
            List<String> hierarchy = explorer.getClassLoaderHierarchy(JvmArchitecture.class);

            assertThat(hierarchy)
                    .isNotEmpty()
                    .last().isEqualTo("Bootstrap (null)");
        }

        @Test
        @DisplayName("Should have at least 2 levels in hierarchy for application classes")
        void testClassLoaderHierarchyDepth() {
            List<String> hierarchy = explorer.getClassLoaderHierarchy(JvmArchitecture.class);
            assertThat(hierarchy).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should correctly identify bootstrap-loaded classes")
        void testIsBootstrapLoaded() {
            assertThat(explorer.isBootstrapLoaded(String.class)).isTrue();
            assertThat(explorer.isBootstrapLoaded(Object.class)).isTrue();
            assertThat(explorer.isBootstrapLoaded(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify non-bootstrap-loaded classes")
        void testIsNotBootstrapLoaded() {
            assertThat(explorer.isBootstrapLoaded(JvmArchitecture.class)).isFalse();
        }

        @Test
        @DisplayName("Should detect same class loader for classes in same module")
        void testSameClassLoader() {
            assertThat(explorer.areSameClassLoader(JvmArchitecture.class, JvmArchitectureTest.class)).isTrue();
        }

        @Test
        @DisplayName("Should detect different class loaders between app and bootstrap")
        void testDifferentClassLoader() {
            assertThat(explorer.areSameClassLoader(String.class, JvmArchitecture.class)).isFalse();
        }

        @Test
        @DisplayName("Should load existing class successfully")
        void testTryLoadClassSuccess() {
            Class<?> clazz = explorer.tryLoadClass("java.lang.String");
            assertThat(clazz).isNotNull().isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should return null for non-existent class")
        void testTryLoadClassNotFound() {
            Class<?> clazz = explorer.tryLoadClass("com.example.NonExistentClass");
            assertThat(clazz).isNull();
        }

        @Test
        @DisplayName("Should check class availability")
        void testIsClassAvailable() {
            assertThat(explorer.isClassAvailable("java.lang.String")).isTrue();
            assertThat(explorer.isClassAvailable("java.util.List")).isTrue();
            assertThat(explorer.isClassAvailable("com.nonexistent.FakeClass")).isFalse();
        }

        @Test
        @DisplayName("Should handle empty class name")
        void testEmptyClassName() {
            assertThat(explorer.isClassAvailable("")).isFalse();
        }
    }

    @Nested
    @DisplayName("Memory Area Explorer")
    class MemoryAreaExplorerTest {

        private final JvmArchitecture.MemoryAreaExplorer explorer = new JvmArchitecture.MemoryAreaExplorer();

        @Test
        @DisplayName("Should return positive heap memory usage")
        void testHeapMemoryUsed() {
            var info = explorer.getHeapMemoryInfo();
            assertThat(info.usedBytes()).isPositive();
            assertThat(info.committedBytes()).isPositive();
            assertThat(info.maxBytes()).isPositive();
        }

        @Test
        @DisplayName("Should have used <= committed <= max for heap")
        void testHeapMemoryOrdering() {
            var info = explorer.getHeapMemoryInfo();
            assertThat(info.usedBytes()).isLessThanOrEqualTo(info.committedBytes());
            assertThat(info.committedBytes()).isLessThanOrEqualTo(info.maxBytes());
        }

        @Test
        @DisplayName("Should return positive non-heap memory usage")
        void testNonHeapMemoryUsed() {
            var info = explorer.getNonHeapMemoryInfo();
            assertThat(info.usedBytes()).isPositive();
            assertThat(info.committedBytes()).isPositive();
        }

        @Test
        @DisplayName("Should return positive max heap bytes")
        void testMaxHeapBytes() {
            assertThat(explorer.getMaxHeapBytes()).isPositive();
        }

        @Test
        @DisplayName("Should return at least 1 available processor")
        void testAvailableProcessors() {
            assertThat(explorer.getAvailableProcessors()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should calculate usage percentage between 0 and 100")
        void testUsagePercentage() {
            var info = explorer.getHeapMemoryInfo();
            assertThat(info.usagePercentage()).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("Should convert to MB correctly")
        void testMBConversion() {
            var info = explorer.getHeapMemoryInfo();
            assertThat(info.usedMB()).isGreaterThanOrEqualTo(0);
            assertThat(info.maxMB()).isPositive();
        }
    }

    @Nested
    @DisplayName("Stack Explorer")
    class StackExplorerTest {

        private final JvmArchitecture.StackExplorer explorer = new JvmArchitecture.StackExplorer();

        @Test
        @DisplayName("Should measure positive max recursion depth")
        void testMaxRecursionDepth() {
            int depth = explorer.measureMaxRecursionDepth();
            assertThat(depth).isPositive();
        }

        @Test
        @DisplayName("Should return positive current stack depth")
        void testCurrentStackDepth() {
            int depth = explorer.getCurrentStackDepth();
            assertThat(depth).isPositive();
        }

        @Test
        @DisplayName("Should demonstrate thread isolation with unique local values")
        void testThreadIsolation() throws InterruptedException {
            List<String> results = explorer.demonstrateThreadIsolation(3);

            assertThat(results).hasSize(3);
            assertThat(results).anyMatch(s -> s.contains("localValue=0"));
            assertThat(results).anyMatch(s -> s.contains("localValue=100"));
            assertThat(results).anyMatch(s -> s.contains("localValue=200"));
        }

        @Test
        @DisplayName("Should handle single thread isolation")
        void testSingleThreadIsolation() throws InterruptedException {
            List<String> results = explorer.demonstrateThreadIsolation(1);
            assertThat(results).hasSize(1);
            assertThat(results.get(0)).contains("Thread-0");
        }

        @Test
        @DisplayName("Should handle zero threads")
        void testZeroThreads() throws InterruptedException {
            List<String> results = explorer.demonstrateThreadIsolation(0);
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Initialization Order")
    class InitializationOrderTest {

        @Test
        @DisplayName("Should execute static block, instance block, and constructor in order")
        void testInitializationOrder() {
            JvmArchitecture.InitializationOrder order = new JvmArchitecture.InitializationOrder();
            List<String> log = order.createMultipleInstances(1);

            // Static block runs first (but may have already run from a previous test)
            // Instance block runs before constructor
            assertThat(log).contains("instance-block", "constructor");

            int instanceIdx = log.indexOf("instance-block");
            int constructorIdx = log.indexOf("constructor");
            assertThat(instanceIdx).isLessThan(constructorIdx);
        }

        @Test
        @DisplayName("Should run static block only once for multiple instances")
        void testStaticBlockRunsOnce() {
            JvmArchitecture.InitializationOrder order = new JvmArchitecture.InitializationOrder();
            List<String> log = order.createMultipleInstances(3);

            // Static block appears at most once (may appear 0 times if already loaded)
            long staticBlockCount = log.stream().filter("static-block"::equals).count();
            assertThat(staticBlockCount).isLessThanOrEqualTo(1);

            // Instance block and constructor appear once per instance
            long instanceBlockCount = log.stream().filter("instance-block"::equals).count();
            long constructorCount = log.stream().filter("constructor"::equals).count();
            assertThat(instanceBlockCount).isEqualTo(3);
            assertThat(constructorCount).isEqualTo(3);
        }

        @Test
        @DisplayName("Should have static field initialized after static block")
        void testStaticFieldInitialized() {
            assertThat(JvmArchitecture.InitializationOrder.DemoClass.STATIC_FIELD)
                    .isEqualTo("initialized");
        }
    }

    @Nested
    @DisplayName("Runtime Info Explorer")
    class RuntimeInfoExplorerTest {

        private final JvmArchitecture.RuntimeInfoExplorer explorer = new JvmArchitecture.RuntimeInfoExplorer();

        @Test
        @DisplayName("Should return non-empty JVM info")
        void testJvmInfo() {
            String info = explorer.getJvmInfo();
            assertThat(info).isNotEmpty();
        }

        @Test
        @DisplayName("Should return a valid Java version")
        void testJavaVersion() {
            String version = explorer.getJavaVersion();
            assertThat(version).isNotEmpty();
            // Java version should start with a number
            assertThat(version).matches("\\d+.*");
        }

        @Test
        @DisplayName("Should return positive uptime")
        void testUptime() {
            long uptime = explorer.getUptimeMs();
            assertThat(uptime).isPositive();
        }

        @Test
        @DisplayName("Should return JVM arguments as a list")
        void testJvmArguments() {
            List<String> args = explorer.getJvmArguments();
            assertThat(args).isNotNull();
            // Args list can be empty or populated depending on test environment
        }

        @Test
        @DisplayName("Should return non-empty class path")
        void testClassPath() {
            String classPath = explorer.getClassPath();
            assertThat(classPath).isNotEmpty();
        }
    }
}
