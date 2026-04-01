package com.github.msorkhpar.claudejavatutor.jvminternals;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates JVM Architecture concepts including Class Loader hierarchy,
 * Runtime Data Areas, and the Execution Engine subsystem.
 */
public class JvmArchitecture {

    /**
     * Demonstrates the class loader hierarchy and parent delegation model.
     */
    public static class ClassLoaderExplorer {

        /**
         * Returns the class loader name for a given class.
         * Bootstrap class loader is represented as null in Java.
         */
        public String getClassLoaderName(Class<?> clazz) {
            ClassLoader loader = clazz.getClassLoader();
            return loader != null ? loader.getName() : "Bootstrap (null)";
        }

        /**
         * Returns the full class loader hierarchy chain for a given class.
         */
        public List<String> getClassLoaderHierarchy(Class<?> clazz) {
            List<String> hierarchy = new ArrayList<>();
            ClassLoader loader = clazz.getClassLoader();
            while (loader != null) {
                hierarchy.add(loader.getName() != null ? loader.getName() : loader.getClass().getName());
                loader = loader.getParent();
            }
            hierarchy.add("Bootstrap (null)");
            return hierarchy;
        }

        /**
         * Checks if a class is loaded by the bootstrap class loader.
         */
        public boolean isBootstrapLoaded(Class<?> clazz) {
            return clazz.getClassLoader() == null;
        }

        /**
         * Demonstrates that classes loaded by different class loaders are different,
         * even if they have the same name.
         */
        public boolean areSameClassLoader(Class<?> clazz1, Class<?> clazz2) {
            return clazz1.getClassLoader() == clazz2.getClassLoader();
        }

        /**
         * Attempts to load a class by name using the current thread's context class loader.
         * Returns the loaded class or null if not found.
         */
        public Class<?> tryLoadClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        /**
         * Checks if a class can be found on the classpath.
         */
        public boolean isClassAvailable(String className) {
            return tryLoadClass(className) != null;
        }
    }

    /**
     * Demonstrates runtime memory area concepts.
     */
    public static class MemoryAreaExplorer {

        /**
         * Returns heap memory usage information.
         */
        public record MemoryInfo(long usedBytes, long committedBytes, long maxBytes) {
            public long usedMB() {
                return usedBytes / (1024 * 1024);
            }

            public long maxMB() {
                return maxBytes / (1024 * 1024);
            }

            public double usagePercentage() {
                if (maxBytes <= 0) return 0.0;
                return 100.0 * usedBytes / maxBytes;
            }
        }

        /**
         * Gets current heap memory information.
         */
        public MemoryInfo getHeapMemoryInfo() {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            return new MemoryInfo(heapUsage.getUsed(), heapUsage.getCommitted(), heapUsage.getMax());
        }

        /**
         * Gets current non-heap (Metaspace + code cache) memory information.
         */
        public MemoryInfo getNonHeapMemoryInfo() {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            return new MemoryInfo(
                    nonHeapUsage.getUsed(),
                    nonHeapUsage.getCommitted(),
                    nonHeapUsage.getMax() > 0 ? nonHeapUsage.getMax() : -1
            );
        }

        /**
         * Gets the maximum heap size configured for the JVM.
         */
        public long getMaxHeapBytes() {
            return Runtime.getRuntime().maxMemory();
        }

        /**
         * Gets the number of available processors.
         */
        public int getAvailableProcessors() {
            return Runtime.getRuntime().availableProcessors();
        }
    }

    /**
     * Demonstrates stack behavior and stack overflow scenarios.
     */
    public static class StackExplorer {

        /**
         * Calculates the approximate maximum recursion depth before StackOverflowError.
         */
        public int measureMaxRecursionDepth() {
            try {
                return recurse(0);
            } catch (StackOverflowError e) {
                return -1; // Will be caught at the first level that can handle it
            }
        }

        private int recurse(int depth) {
            try {
                return recurse(depth + 1);
            } catch (StackOverflowError e) {
                return depth;
            }
        }

        /**
         * Gets the current thread's stack trace depth.
         */
        public int getCurrentStackDepth() {
            return Thread.currentThread().getStackTrace().length;
        }

        /**
         * Demonstrates that each thread has its own stack by running concurrent
         * operations that each use their own local variables.
         */
        public List<String> demonstrateThreadIsolation(int threadCount) throws InterruptedException {
            List<String> results = java.util.Collections.synchronizedList(new ArrayList<>());
            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                Thread t = new Thread(() -> {
                    // Each thread has its own stack with its own local variable
                    int localValue = threadId * 100;
                    results.add("Thread-" + threadId + ": localValue=" + localValue);
                });
                threads.add(t);
                t.start();
            }

            for (Thread t : threads) {
                t.join();
            }

            return results;
        }
    }

    /**
     * Demonstrates static initialization and class loading order.
     */
    public static class InitializationOrder {

        private static final List<String> initLog = new ArrayList<>();

        /**
         * Records an initialization event.
         */
        public static void recordInit(String event) {
            initLog.add(event);
        }

        /**
         * Returns the initialization log.
         */
        public static List<String> getInitLog() {
            return new ArrayList<>(initLog);
        }

        /**
         * Clears the initialization log.
         */
        public static void clearLog() {
            initLog.clear();
        }

        /**
         * Simulates class initialization order: static fields, static blocks, constructors.
         */
        public static class DemoClass {
            static final String STATIC_FIELD;

            static {
                STATIC_FIELD = "initialized";
                recordInit("static-block");
            }

            {
                recordInit("instance-block");
            }

            public DemoClass() {
                recordInit("constructor");
            }
        }

        /**
         * Demonstrates that static initialization happens only once,
         * regardless of how many instances are created.
         */
        public List<String> createMultipleInstances(int count) {
            clearLog();
            for (int i = 0; i < count; i++) {
                new DemoClass();
            }
            return getInitLog();
        }
    }

    /**
     * Demonstrates JVM runtime information retrieval.
     */
    public static class RuntimeInfoExplorer {

        /**
         * Gets the JVM name and version.
         */
        public String getJvmInfo() {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            return runtime.getVmName() + " " + runtime.getVmVersion();
        }

        /**
         * Gets the Java specification version.
         */
        public String getJavaVersion() {
            return System.getProperty("java.version");
        }

        /**
         * Gets the JVM uptime in milliseconds.
         */
        public long getUptimeMs() {
            return ManagementFactory.getRuntimeMXBean().getUptime();
        }

        /**
         * Gets the JVM input arguments (command line flags).
         */
        public List<String> getJvmArguments() {
            return ManagementFactory.getRuntimeMXBean().getInputArguments();
        }

        /**
         * Gets the class path.
         */
        public String getClassPath() {
            return System.getProperty("java.class.path");
        }
    }
}
