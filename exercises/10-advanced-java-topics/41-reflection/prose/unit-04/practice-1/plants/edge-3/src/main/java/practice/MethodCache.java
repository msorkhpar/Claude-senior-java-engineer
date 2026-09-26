package practice;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MethodCache {

    /** Does the real lookup. */
    public interface Finder {
        Method find(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException;
    }

    private final Finder finder;
    private final Map<String, Method> cache = new ConcurrentHashMap<>();

    public MethodCache(Finder finder) {
        this.finder = finder;
    }

    /** The cached method, looked up through the finder on first request. */
    public Method get(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException {
        String key = type.getName() + "#" + name + Arrays.toString(params);
        Method method = cache.get(key);
        if (method == null) {
            method = finder.find(type, name, params);
            cache.put(key, method);
        }
        return method;
    }

    /** How many methods are cached. */
    public int size() {
        return cache.size();
    }
}
