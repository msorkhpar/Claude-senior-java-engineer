package practice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public final class MethodFinder {

    private MethodFinder() {
    }

    /** The method with exactly these parameter types on type or a superclass, any access level. */
    public static Optional<Method> find(Class<?> type, String name, Class<?>... params) {
        throw new UnsupportedOperationException("TODO");
    }

    /** "name(Type,Type)" for every method type declares with that name, sorted. */
    public static List<String> overloads(Class<?> type, String name) {
        throw new UnsupportedOperationException("TODO");
    }
}
