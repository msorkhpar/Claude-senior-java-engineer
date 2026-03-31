package com.github.msorkhpar.claudejavatutor.reflection;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Demonstrates dynamic object creation and method invocation using Java Reflection.
 * Covers creating objects from class names, invoking methods dynamically, working
 * with arrays reflectively, implementing dynamic proxies, and building a simple
 * dependency injection container.
 */
public class DynamicCreationInvocation {

    // --- Sample interfaces and classes ---

    public interface Greeter {
        String greet(String name);
    }

    public interface Calculator {
        int compute(int a, int b);
    }

    public static class EnglishGreeter implements Greeter {
        @Override
        public String greet(String name) {
            return "Hello, " + name + "!";
        }
    }

    public static class FrenchGreeter implements Greeter {
        @Override
        public String greet(String name) {
            return "Bonjour, " + name + "!";
        }
    }

    public static class Addition implements Calculator {
        @Override
        public int compute(int a, int b) {
            return a + b;
        }
    }

    public static class Multiplication implements Calculator {
        @Override
        public int compute(int a, int b) {
            return a * b;
        }
    }

    public static class Service {
        private final String name;

        public Service() {
            this.name = "DefaultService";
        }

        public Service(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String process(String input) {
            return name + " processed: " + input;
        }
    }

    // --- Dynamic object creation ---

    /**
     * Creates an instance from a fully-qualified class name using the no-arg constructor.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createFromClassName(String className) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (T) constructor.newInstance();
    }

    /**
     * Creates an instance from a class name with constructor arguments.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createFromClassName(String className, Class<?>[] paramTypes, Object... args)
            throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return (T) constructor.newInstance(args);
    }

    /**
     * Dynamically invokes a method on a target object by method name.
     */
    public static Object invokeDynamic(Object target, String methodName, Object... args)
            throws ReflectiveOperationException {
        Class<?>[] paramTypes = Arrays.stream(args)
                .map(Object::getClass)
                .toArray(Class<?>[]::new);

        // Handle primitive type boxing: int.class vs Integer.class
        Method method = findMethod(target.getClass(), methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>[] argTypes) throws NoSuchMethodException {
        // Try exact match first
        try {
            return clazz.getDeclaredMethod(name, argTypes);
        } catch (NoSuchMethodException e) {
            // Try with primitive unboxing
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == argTypes.length) {
                    Class<?>[] paramTypes = m.getParameterTypes();
                    boolean match = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (!isAssignable(paramTypes[i], argTypes[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return m;
                    }
                }
            }
            throw new NoSuchMethodException(name);
        }
    }

    private static boolean isAssignable(Class<?> target, Class<?> source) {
        if (target.isAssignableFrom(source)) return true;
        // Handle primitive/wrapper equivalences
        Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
                int.class, Integer.class,
                long.class, Long.class,
                double.class, Double.class,
                float.class, Float.class,
                boolean.class, Boolean.class,
                byte.class, Byte.class,
                short.class, Short.class,
                char.class, Character.class
        );
        Class<?> wrapped = primitiveToWrapper.get(target);
        return wrapped != null && wrapped.isAssignableFrom(source);
    }

    // --- Dynamic array creation ---

    /**
     * Creates an array of the given component type and size dynamically.
     */
    public static Object createArray(Class<?> componentType, int size) {
        return Array.newInstance(componentType, size);
    }

    /**
     * Sets a value in a reflectively created array.
     */
    public static void setArrayElement(Object array, int index, Object value) {
        Array.set(array, index, value);
    }

    /**
     * Gets a value from a reflectively created array.
     */
    public static Object getArrayElement(Object array, int index) {
        return Array.get(array, index);
    }

    /**
     * Returns the length of a reflectively created array.
     */
    public static int getArrayLength(Object array) {
        return Array.getLength(array);
    }

    // --- Dynamic Proxy ---

    /**
     * Creates a logging proxy that logs method calls before delegating to the real object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createLoggingProxy(Class<T> interfaceType, T realObject, List<String> log) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    String argStr = args != null
                            ? Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", "))
                            : "";
                    log.add("Calling " + method.getName() + "(" + argStr + ")");
                    Object result = method.invoke(realObject, args);
                    log.add("Returned: " + result);
                    return result;
                }
        );
    }

    /**
     * Creates a proxy that measures method execution time.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createTimingProxy(Class<T> interfaceType, T realObject, Map<String, Long> timings) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                (proxy, method, args) -> {
                    long start = System.nanoTime();
                    Object result = method.invoke(realObject, args);
                    long elapsed = System.nanoTime() - start;
                    timings.put(method.getName(), elapsed);
                    return result;
                }
        );
    }

    // --- Simple Factory using reflection ---

    /**
     * A simple reflective factory that registers and creates objects by key.
     */
    public static class ReflectiveFactory<T> {
        private final Map<String, Class<? extends T>> registry = new HashMap<>();

        public void register(String key, Class<? extends T> clazz) {
            registry.put(key, clazz);
        }

        public T create(String key) throws ReflectiveOperationException {
            Class<? extends T> clazz = registry.get(key);
            if (clazz == null) {
                throw new IllegalArgumentException("No class registered for key: " + key);
            }
            Constructor<? extends T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }

    // --- Simple DI Container ---

    /**
     * A minimal dependency injection container that creates and wires objects using reflection.
     */
    public static class SimpleDIContainer {
        private final Map<Class<?>, Supplier<?>> bindings = new HashMap<>();
        private final Map<Class<?>, Object> singletons = new HashMap<>();

        public <T> void bind(Class<T> type, Supplier<T> supplier) {
            bindings.put(type, supplier);
        }

        public <T> void bindSingleton(Class<T> type, Supplier<T> supplier) {
            bindings.put(type, () -> {
                if (!singletons.containsKey(type)) {
                    singletons.put(type, supplier.get());
                }
                return singletons.get(type);
            });
        }

        @SuppressWarnings("unchecked")
        public <T> T resolve(Class<T> type) {
            Supplier<?> supplier = bindings.get(type);
            if (supplier != null) {
                return (T) supplier.get();
            }
            throw new IllegalArgumentException("No binding for: " + type.getName());
        }
    }
}
