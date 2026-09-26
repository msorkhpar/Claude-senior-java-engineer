package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Access {

    private Access() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Roles {
        Role[] value();
    }

    @Repeatable(Roles.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Role {
        String value();
    }

    /** The role names on e, in the order written. */
    public static List<String> rolesOf(AnnotatedElement e) {
        List<String> names = new ArrayList<>();
        Roles container = e.getAnnotation(Roles.class);
        if (container != null) {
            for (Role role : container.value()) {
                names.add(role.value());
            }
        }
        return names;
    }

    /** Whether a caller holding the granted roles may call m. */
    public static boolean canCall(Method m, Set<String> granted) {
        List<String> required = rolesOf(m);
        if (required.isEmpty()) {
            return true;
        }
        for (String role : required) {
            if (granted.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
