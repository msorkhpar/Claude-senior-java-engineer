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
        throw new UnsupportedOperationException("TODO");
    }
}
