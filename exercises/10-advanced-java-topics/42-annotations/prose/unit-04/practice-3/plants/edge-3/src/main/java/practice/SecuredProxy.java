package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

public final class SecuredProxy {

    private SecuredProxy() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RequiresPermission {
        String value();
    }

    /** A proxy of iface that checks @RequiresPermission on target's methods before forwarding. */
    public static <T> T secure(T target, Class<T> iface, Set<String> permissions) {
        Object proxy = Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, (p, method, args) -> {
            Method impl = null;
            for (Method m : target.getClass().getMethods()) {
                if (m.getName().equals(method.getName()) && (impl == null || m.getParameterTypes()[0] == String.class)) {
                    impl = m;
                }
            }
            RequiresPermission rule = impl.getAnnotation(RequiresPermission.class);
            if (rule != null && !permissions.contains(rule.value())) {
                throw new SecurityException("Missing permission: " + rule.value());
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
        return iface.cast(proxy);
    }
}
