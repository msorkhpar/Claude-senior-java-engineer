package practice;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Bridges {

    private Bridges() {
    }

    /** The bridge methods {@code type} declares, as "ReturnType name(Param, ...)", in any order. */
    public static List<String> bridgesOf(Class<?> type) {
        return describe(type, Method::isSynthetic);
    }

    /** The methods the source of {@code type} declares (no synthetic ones), in the same form. */
    public static List<String> sourceMethodsOf(Class<?> type) {
        return describe(type, m -> !m.isSynthetic());
    }

    private static List<String> describe(Class<?> type, Predicate<Method> keep) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(keep)
                .map(m -> m.getReturnType().getSimpleName() + " " + m.getName() + "("
                        + Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName)
                                .collect(Collectors.joining(", ")) + ")")
                .toList();
    }
}
