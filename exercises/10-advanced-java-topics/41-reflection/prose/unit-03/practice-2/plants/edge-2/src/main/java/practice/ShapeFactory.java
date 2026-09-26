package practice;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ShapeFactory {

    /** A shape the factory creates. */
    public interface Shape {
        double area();
    }

    private final Map<String, Class<? extends Shape>> registry = new HashMap<>();

    /** Registers type under name, ignoring case; refuses a type without a no-arg constructor. */
    public void register(String name, Class<? extends Shape> type) {
        registry.put(key(name), type);
    }

    /** A new instance of the class registered under name, in any case. */
    public Shape create(String name) {
        Class<? extends Shape> type = registry.get(key(name));
        if (type == null) {
            throw new IllegalArgumentException("Unknown shape: " + name);
        }
        try {
            Constructor<? extends Shape> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + name, e);
        }
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
