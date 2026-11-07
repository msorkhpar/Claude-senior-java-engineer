package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Decides whether a caller may invoke an endpoint method of the billing API.
 *
 * An endpoint lists the permissions that let a caller in; holding any one of them is enough.
 * A method without permissions of its own uses its class's. An endpoint with none at all,
 * on the method or on its class, is public.
 */
public class PermissionGuard {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Repeatable(Permissions.class)
    public @interface Permission {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Permissions {
        Permission[] value();
    }

    /** Whether a caller holding {@code granted} may invoke {@code endpoint}. */
    public boolean mayCall(Method endpoint, Set<String> granted) {
        Set<String> required = required(endpoint);
        if (required.isEmpty()) return true;
        for (String permission : required) {
            if (granted.contains(permission)) return true;
        }
        return false;
    }

    /** The permissions that let a caller into {@code endpoint}, in the order they are written. */
    Set<String> required(Method endpoint) {
        Permissions own = endpoint.getAnnotation(Permissions.class);
        Set<String> names = own == null ? new LinkedHashSet<>() : namesOn(own.value());
        if (names.isEmpty()) {
            names = namesOn(endpoint.getDeclaringClass().getAnnotationsByType(Permission.class));
        }
        return names;
    }

    private static Set<String> namesOn(Permission[] permissions) {
        Set<String> names = new LinkedHashSet<>();
        for (Permission p : permissions) names.add(p.value());
        return names;
    }
}
