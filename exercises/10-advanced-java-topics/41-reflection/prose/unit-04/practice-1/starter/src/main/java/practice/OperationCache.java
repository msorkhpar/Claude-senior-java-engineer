package practice;

import java.lang.reflect.Method;
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
        // TODO: Return the method for this operation, ready to invoke even when it is private.
        //       The console serves thousands of requests a minute for a few dozen operations.
        throw new IllegalStateException("TODO: method");
    }

    /** The key the method for this operation is cached under. */
    Object key(Class<?> type, String name, Class<?>... params) {
        // TODO: A key that two requests share exactly when they need the same method.
        throw new IllegalStateException("TODO: key");
    }
}
