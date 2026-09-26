package practice;

import java.util.List;
import java.util.Set;

public final class Hierarchy {

    private Hierarchy() {
    }

    /** The type and every superclass above it, nearest first. */
    public static List<Class<?>> superclasses(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Every interface of the type: from its superclasses and super-interfaces too. */
    public static Set<Class<?>> allInterfaces(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
