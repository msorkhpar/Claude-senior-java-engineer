package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Tracing {

    private Tracing() {
    }

    public interface Store {
        String get(String key);

        String put(String key, String value);

        int size();
    }

    /** Returns a proxy of {@code type} that logs each call into {@code log} and delegates it to {@code target}. */
    public static <T> T logging(T target, Class<T> type, List<String> log) {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(log, "log must not be null");
        InvocationHandler handler = (proxy, method, args) -> {
            if (!isObjectMethod(method)) {
                log.add(method.getName() + "(" + describe(args) + ")");
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    private static String describe(Object[] args) {
        String all = Arrays.toString(args);
        return args == null ? all : all.substring(1, all.length() - 1);
    }
}
