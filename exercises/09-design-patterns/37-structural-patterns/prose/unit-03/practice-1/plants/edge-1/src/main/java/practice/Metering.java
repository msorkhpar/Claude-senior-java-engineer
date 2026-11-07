package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Counts the calls made on any interface, per method name, for the fare service's dashboard.
 * The service wraps every fare listener in a metered proxy before it subscribes it.
 */
public final class Metering {

    private Metering() {
    }

    /** Told whenever the price of a route changes. */
    public interface FareListener {
        void fareChanged(String route, long cents);
    }

    /** The service's subscriber list. */
    public static final class FareBoard {
        private final List<FareListener> listeners = new ArrayList<>();

        public void subscribe(FareListener listener) {
            listeners.add(listener);
        }

        public boolean unsubscribe(FareListener listener) {
            return listeners.remove(listener);
        }

        public void publish(String route, long cents) {
            for (FareListener listener : List.copyOf(listeners)) listener.fareChanged(route, cents);
        }
    }

    /** Returns a proxy of {@code type} that counts each call into {@code counts} and passes it to {@code target}. */
    public static <T> T metered(T target, Class<T> type, Map<String, Integer> counts) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (isObjectMethod(method)) return objectMethod(proxy, method, args, target);
            counts.merge(method.getName(), 1, Integer::sum);
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    /** Whether the proxy should answer this call itself instead of counting it and passing it on. */
    static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    /** The proxy's own answer to a call that isObjectMethod picked out. */
    static Object objectMethod(Object proxy, Method method, Object[] args, Object target) {
        return switch (method.getName()) {
            case "equals" -> target.equals(args[0]);
            case "hashCode" -> target.hashCode();
            default -> "metered " + target;
        };
    }
}
