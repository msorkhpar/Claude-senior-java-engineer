package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public final class Maker {

    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            int.class, Integer.class, long.class, Long.class, double.class, Double.class,
            float.class, Float.class, boolean.class, Boolean.class, byte.class, Byte.class,
            short.class, Short.class, char.class, Character.class);

    private Maker() {
    }

    /** Calls the constructor of type whose parameters fit args, at any access level. */
    public static <T> T make(Class<T> type, Object... args) throws ReflectiveOperationException {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (fits(constructor.getParameterTypes(), args)) {
                constructor.setAccessible(true);
                try {
                    return type.cast(constructor.newInstance(args));
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException unchecked) {
                        throw unchecked;
                    }
                    throw new IllegalStateException(cause);
                }
            }
        }
        throw new NoSuchMethodException("no constructor of " + type.getName() + " fits the arguments");
    }

    private static boolean fits(Class<?>[] parameters, Object[] args) {
        if (parameters.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameters.length; i++) {
            Class<?> expected = parameters[i];
            if (!expected.isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }
}
