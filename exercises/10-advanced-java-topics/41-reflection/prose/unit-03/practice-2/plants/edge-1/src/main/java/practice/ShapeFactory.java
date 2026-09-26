package practice;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public final class ShapeFactory {

    /** A shape the factory creates. */
    public interface Shape {
        double area();
    }

    private final Map<String, Constructor<? extends Shape>> registry = new HashMap<>();

    /** Registers type under name, ignoring case; refuses a type without a no-arg constructor. */
    public void register(String name, Class<? extends Shape> type) {
        try {
            Constructor<? extends Shape> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            registry.put(key(name), constructor);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type.getName() + " has no no-arg constructor", e);
        }
    }

    /** A new instance of the class registered under name, in any case. */
    public Shape create(String name) {
        Constructor<? extends Shape> constructor = registry.get(key(name));
        if (constructor == null) {
            throw new IllegalArgumentException("Unknown shape: " + name);
        }
        try {
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + name, e);
        }
    }

    private static String key(String name) {
        return name.toLowerCase();
    }
}
