package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MiniContainer {

    /** Marks the constructor the container uses. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.CONSTRUCTOR)
    public @interface Inject {
    }

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Set<Class<?>> creating = new HashSet<>();

    /** The one instance of type, created with its dependencies on first request. */
    public <T> T resolve(Class<T> type) {
        Object existing = singletons.get(type);
        if (existing != null) {
            return type.cast(existing);
        }
        if (!creating.add(type)) {
            throw new IllegalStateException("dependency cycle at " + type.getName());
        }
        try {
            Constructor<?> constructor = constructorOf(type);
            Class<?>[] parameters = constructor.getParameterTypes();
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                args[i] = resolve(parameters[i]);
            }
            constructor.setAccessible(true);
            T instance = type.cast(constructor.newInstance(args));
            singletons.put(type, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + type.getName(), e);
        } finally {
            creating.remove(type);
        }
    }

    private static Constructor<?> constructorOf(Class<?> type) throws NoSuchMethodException {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return constructor;
            }
        }
        return type.getDeclaredConstructor();
    }
}
