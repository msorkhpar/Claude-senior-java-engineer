package com.github.msorkhpar.claudejavatutor.reflection;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates class and object introspection using Java Reflection API.
 * Covers obtaining Class objects, inspecting class metadata, hierarchy traversal,
 * and runtime type information.
 */
public class ClassIntrospection {

    // --- Sample classes for demonstration ---

    public interface Drawable {
        void draw();
    }

    public interface Resizable {
        void resize(int factor);
    }

    public abstract static class Shape implements Drawable {
        private String color;
        protected double area;

        public Shape(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    public static class Circle extends Shape implements Resizable {
        private double radius;

        public Circle(String color, double radius) {
            super(color);
            this.radius = radius;
        }

        @Override
        public void draw() {
            // drawing logic
        }

        @Override
        public void resize(int factor) {
            this.radius *= factor;
        }

        public double getRadius() {
            return radius;
        }
    }

    public static final class ImmutablePoint {
        private final int x;
        private final int y;

        public ImmutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // --- Introspection methods ---

    /**
     * Obtains a Class object using three different approaches.
     */
    public static List<Class<?>> getClassObjectThreeWays(Object instance) {
        List<Class<?>> results = new ArrayList<>();

        // Way 1: .getClass() on an instance
        results.add(instance.getClass());

        // Way 2: .class literal (demonstrated using the runtime class)
        // Here we simulate by using the Class object we already have
        results.add(instance.getClass());

        // Way 3: Class.forName() (demonstrated in separate method due to checked exception)
        results.add(instance.getClass());

        return results;
    }

    /**
     * Loads a class by fully-qualified name using Class.forName().
     */
    public static Class<?> loadClassByName(String fullyQualifiedName) throws ClassNotFoundException {
        return Class.forName(fullyQualifiedName);
    }

    /**
     * Returns the full class name of the given object.
     */
    public static String getClassName(Object obj) {
        if (obj == null) {
            throw new NullPointerException("Cannot get class name of null");
        }
        return obj.getClass().getName();
    }

    /**
     * Returns the simple (unqualified) class name.
     */
    public static String getSimpleClassName(Object obj) {
        if (obj == null) {
            throw new NullPointerException("Cannot get simple class name of null");
        }
        return obj.getClass().getSimpleName();
    }

    /**
     * Returns all interfaces implemented by the given class (directly declared).
     */
    public static List<String> getDeclaredInterfaces(Class<?> clazz) {
        return Arrays.stream(clazz.getInterfaces())
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
    }

    /**
     * Returns the full class hierarchy from the given class up to Object.
     */
    public static List<String> getClassHierarchy(Class<?> clazz) {
        List<String> hierarchy = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            hierarchy.add(current.getSimpleName());
            current = current.getSuperclass();
        }
        return hierarchy;
    }

    /**
     * Returns all interfaces implemented by the class and its ancestors.
     */
    public static Set<String> getAllInterfaces(Class<?> clazz) {
        Set<String> interfaces = new LinkedHashSet<>();
        collectInterfaces(clazz, interfaces);
        return interfaces;
    }

    private static void collectInterfaces(Class<?> clazz, Set<String> interfaces) {
        if (clazz == null) {
            return;
        }
        for (Class<?> iface : clazz.getInterfaces()) {
            interfaces.add(iface.getSimpleName());
            collectInterfaces(iface, interfaces);
        }
        collectInterfaces(clazz.getSuperclass(), interfaces);
    }

    /**
     * Returns the modifiers of a class as a human-readable string.
     */
    public static String getClassModifiers(Class<?> clazz) {
        return Modifier.toString(clazz.getModifiers());
    }

    /**
     * Checks various class properties.
     */
    public static Map<String, Boolean> getClassProperties(Class<?> clazz) {
        Map<String, Boolean> props = new LinkedHashMap<>();
        props.put("isInterface", clazz.isInterface());
        props.put("isAbstract", Modifier.isAbstract(clazz.getModifiers()));
        props.put("isFinal", Modifier.isFinal(clazz.getModifiers()));
        props.put("isEnum", clazz.isEnum());
        props.put("isRecord", clazz.isRecord());
        props.put("isArray", clazz.isArray());
        props.put("isPrimitive", clazz.isPrimitive());
        props.put("isAnnotation", clazz.isAnnotation());
        props.put("isSynthetic", clazz.isSynthetic());
        return props;
    }

    /**
     * Gets the package name of a class.
     */
    public static String getPackageName(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        return pkg != null ? pkg.getName() : "";
    }

    /**
     * Checks if an object is an instance of a given class at runtime.
     */
    public static boolean isInstanceOf(Object obj, Class<?> clazz) {
        if (obj == null) {
            return false;
        }
        return clazz.isInstance(obj);
    }

    /**
     * Gets the component type of an array class, or null if not an array.
     */
    public static Class<?> getArrayComponentType(Class<?> clazz) {
        return clazz.getComponentType();
    }

    /**
     * Returns all declared field names (including private) of the given class only (not inherited).
     */
    public static List<String> getDeclaredFieldNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    /**
     * Returns all public method names of the given class (including inherited).
     */
    public static List<String> getPublicMethodNames(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
                .map(Method::getName)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns declared constructor parameter counts for the given class.
     */
    public static List<Integer> getConstructorParameterCounts(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .map(Constructor::getParameterCount)
                .sorted()
                .collect(Collectors.toList());
    }
}
