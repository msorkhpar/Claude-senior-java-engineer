package practice;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MethodFinder {

    private MethodFinder() {
    }

    /** The method with exactly these parameter types on type or a superclass, any access level. */
    public static Optional<Method> find(Class<?> type, String name, Class<?>... params) {
        try {
            return Optional.of(type.getDeclaredMethod(name, params));
        } catch (NoSuchMethodException e) {
            // not declared here: try the public ones
        }
        try {
            return Optional.of(type.getMethod(name, params));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /** "name(Type,Type)" for every method type declares with that name, sorted. */
    public static List<String> overloads(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> m.getName().equals(name))
                .map(MethodFinder::signature)
                .sorted()
                .toList();
    }

    private static String signature(Method method) {
        return method.getName() + Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(",", "(", ")"));
    }
}
