package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public final class PluginLoader {

    /** What every plugin does. */
    public interface Plugin {
        String execute();
    }

    private PluginLoader() {
    }

    /** Creates a new instance of the named Plugin class with its no-arg constructor. */
    public static Plugin load(String className) {
        Class<?> type;
        try {
            type = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("unknown plugin class " + className, e);
        }
        if (!Plugin.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(className + " is not a Plugin");
        }
        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException(className + " cannot be instantiated");
        }
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            return (Plugin) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + className, e);
        }
    }
}
