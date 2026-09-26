package practice;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Hierarchy {

    private Hierarchy() {
    }

    /** The type and every superclass above it, nearest first. */
    public static List<Class<?>> superclasses(Class<?> type) {
        List<Class<?>> chain = new ArrayList<>();
        Class<?> current = type;
        while (current != null) {
            chain.add(current);
            current = current.getSuperclass();
        }
        return chain;
    }

    /** Every interface of the type: from its superclasses and super-interfaces too. */
    public static Set<Class<?>> allInterfaces(Class<?> type) {
        Set<Class<?>> found = new LinkedHashSet<>();
        collect(type, found);
        return found;
    }

    private static void collect(Class<?> type, Set<Class<?>> found) {
        if (type == null) {
            return;
        }
        for (Class<?> each : type.getInterfaces()) {
            found.add(each);
        }
        collect(type.getSuperclass(), found);
    }
}
