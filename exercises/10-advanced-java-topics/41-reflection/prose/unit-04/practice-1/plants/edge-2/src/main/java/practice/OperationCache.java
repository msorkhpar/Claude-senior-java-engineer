package practice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The admin console runs operations on live service objects by name: "resize" with an int on the
 * thread pool, "restart" on a worker. Every request finds the operation's method through this
 * class.
 */
public class OperationCache {

    /** Finds a method a class declares. In production this is Class::getDeclaredMethod. */
    public interface Finder {
        Method find(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException;
    }

    private final Finder finder;
    private final Map<Object, Method> methods = new ConcurrentHashMap<>();

    public OperationCache(Finder finder) {
        this.finder = finder;
    }

    public OperationCache() {
        this(Class::getDeclaredMethod);
    }

    /** Runs one request: the named operation on the target, with these parameter types and arguments. */
    public Object run(Object target, String name, Class<?>[] params, Object... args)
            throws ReflectiveOperationException {
        return method(target.getClass(), name, params).invoke(target, args);
    }

    /** Number of methods the cache holds. */
    public int size() {
        return methods.size();
    }

    /** The method that runs this operation, ready to invoke whatever its access level. */
    Method method(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException {
        Object key = key(type, name, params);
        Method m = methods.get(key);
        if (m == null) {
            m = finder.find(type, name, params);
            m.setAccessible(true);
            methods.put(key, m);
        }
        return m;
    }

    /** The key the method for this operation is cached under. */
    Object key(Class<?> type, String name, Class<?>... params) {
        return List.of(type.getSimpleName(), name, List.of(params));
    }
}
