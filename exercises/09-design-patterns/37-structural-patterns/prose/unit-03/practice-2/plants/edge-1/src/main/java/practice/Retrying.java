package practice;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wraps a remote client so that a call that fails on the network is tried again. The billing
 * service wraps its exchange-rate client with it.
 */
public final class Retrying {

    private Retrying() {
    }

    /** The exchange-rate service, reached over the network. */
    public interface Rates {
        /** The rate for a currency pair such as "EUR/CHF", in millionths. */
        long rate(String pair) throws IOException;
    }

    /** Returns a proxy of {@code type} that calls {@code target} up to {@code attempts} times per call. */
    public static <T> T wrap(T target, Class<T> type, int attempts) {
        if (attempts < 1) throw new IllegalArgumentException("attempts must be at least 1");
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> call(target, method, args, attempts)));
    }

    /** Makes one call on the proxy happen: calls {@code method} on {@code target}, again if it is worth it. */
    static Object call(Object target, Method method, Object[] args, int attempts) throws Throwable {
        for (int attempt = 1; ; attempt++) {
            try {
                return method.invoke(target, args);
            } catch (Exception e) {
                if (!(e instanceof IOException) || attempt == attempts) throw e;
            }
        }
    }
}
