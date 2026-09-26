package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Tracing {

    private Tracing() {
    }

    /** A proxy of iface that calls target and logs every call and its outcome. */
    public static <T> T trace(Class<T> iface, T target, List<String> log) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    default -> "Traced(" + target + ")";
                };
            }
            Object[] actual = args == null ? new Object[0] : args;
            log.add(method.getName() + Arrays.stream(actual)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ", "(", ")")));
            try {
                Object result = method.invoke(target, actual);
                log.add(method.getName() + " -> " + result);
                return result;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                log.add(method.getName() + " !! " + cause.getClass().getSimpleName());
                throw e;
            }
        };
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, handler));
    }
}
