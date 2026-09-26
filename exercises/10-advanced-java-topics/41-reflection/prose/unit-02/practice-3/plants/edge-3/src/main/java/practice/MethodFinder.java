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
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return Optional.of(current.getDeclaredMethod(name, params));
            } catch (NoSuchMethodException e) {
                // not declared here: try the parent
            }
        }
        return Optional.empty();
    }

    /** "name(Type,Type)" for every method type declares with that name, sorted. */
    public static List<String> overloads(Class<?> type, String name) {
        return Arrays.stream(type.getMethods())
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
