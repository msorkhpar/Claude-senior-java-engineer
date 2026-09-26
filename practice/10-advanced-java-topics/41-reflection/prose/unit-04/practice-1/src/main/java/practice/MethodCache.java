package practice;

import java.lang.reflect.Method;

public final class MethodCache {

    /** Does the real lookup. */
    public interface Finder {
        Method find(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException;
    }

    public MethodCache(Finder finder) {
    }

    /** The cached method, looked up through the finder on first request. */
    public Method get(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException {
        throw new UnsupportedOperationException("TODO");
    }

    /** How many methods are cached. */
    public int size() {
        throw new UnsupportedOperationException("TODO");
    }
}
