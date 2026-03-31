package com.github.msorkhpar.claudejavatutor.reflection;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates accessing fields, methods, and constructors via Java Reflection API.
 * Covers reading/writing private fields, invoking methods, and constructing objects
 * using Constructor objects.
 */
public class FieldMethodConstructorAccess {

    // --- Sample classes ---

    public static class Person {
        private String name;
        private int age;
        public String email;
        protected String nickname;
        private static int instanceCount = 0;

        public Person() {
            this.name = "Unknown";
            this.age = 0;
            instanceCount++;
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
            instanceCount++;
        }

        private Person(String name) {
            this.name = name;
            this.age = -1;
            instanceCount++;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        private String formatInfo() {
            return name + " (" + age + ")";
        }

        private static int getInstanceCount() {
            return instanceCount;
        }

        public static void resetCount() {
            instanceCount = 0;
        }

        public String greet(String greeting) {
            return greeting + ", " + name + "!";
        }

        public int add(int a, int b) {
            return a + b;
        }

        private String secretMethod(String secret) {
            return "Secret: " + secret;
        }
    }

    public static class ImmutableConfig {
        private final String host;
        private final int port;

        public ImmutableConfig(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    // --- Field access methods ---

    /**
     * Reads a field value by name, including private fields.
     */
    public static Object readField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    /**
     * Writes a field value by name, including private fields.
     */
    public static void writeField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Reads a static field value by name.
     */
    public static Object readStaticField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    /**
     * Returns all fields with their types and modifiers for a given class.
     */
    public static List<String> describeFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(f -> String.format("%s %s %s",
                        Modifier.toString(f.getModifiers()),
                        f.getType().getSimpleName(),
                        f.getName()))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Returns fields filtered by a specific modifier (e.g., Modifier.PRIVATE).
     */
    public static List<String> getFieldsByModifier(Class<?> clazz, int modifier) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> (f.getModifiers() & modifier) != 0)
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    // --- Method access ---

    /**
     * Invokes a method by name on the target object with given arguments.
     */
    public static Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    /**
     * Invokes a static method by name.
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    /**
     * Returns all declared methods with their signatures.
     */
    public static List<String> describeMethodSignatures(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(m -> {
                    String params = Arrays.stream(m.getParameterTypes())
                            .map(Class::getSimpleName)
                            .collect(Collectors.joining(", "));
                    return String.format("%s %s(%s)",
                            m.getReturnType().getSimpleName(),
                            m.getName(),
                            params);
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns method parameter names (requires compilation with -parameters flag).
     */
    public static List<String> getMethodParameterNames(Class<?> clazz, String methodName) throws NoSuchMethodException {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return Arrays.stream(m.getParameters())
                        .map(Parameter::getName)
                        .collect(Collectors.toList());
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    // --- Constructor access ---

    /**
     * Creates an instance using the no-arg constructor.
     */
    public static <T> T createInstanceNoArg(Class<T> clazz)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * Creates an instance using a constructor with specified parameter types and arguments.
     */
    public static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args)
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    /**
     * Describes all constructors of a class.
     */
    public static List<String> describeConstructors(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .map(c -> {
                    String params = Arrays.stream(c.getParameterTypes())
                            .map(Class::getSimpleName)
                            .collect(Collectors.joining(", "));
                    String mods = Modifier.toString(c.getModifiers());
                    return String.format("%s %s(%s)", mods, c.getDeclaringClass().getSimpleName(), params).trim();
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Modifies a final field value using reflection (for demonstration -- not recommended in production).
     */
    public static void writeFinalField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
