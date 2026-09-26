package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ShapeCatalog {

    private ShapeCatalog() {
    }

    /** "name: Type" for each component of a record, in declaration order. */
    public static List<String> components(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !java.lang.reflect.Modifier.isStatic(f.getModifiers()))
                .map(f -> f.getName() + ": " + f.getType().getSimpleName())
                .toList();
    }

    /** The sorted simple names of every non-sealed type below a sealed type. */
    public static List<String> leaves(Class<?> type) {
        if (!type.isSealed()) {
            throw new IllegalArgumentException(type.getName() + " is not sealed");
        }
        List<String> found = new ArrayList<>();
        collect(type, found);
        return found.stream().sorted().toList();
    }

    private static void collect(Class<?> sealed, List<String> found) {
        for (Class<?> permitted : sealed.getPermittedSubclasses()) {
            if (permitted.isSealed()) {
                collect(permitted, found);
            } else {
                found.add(permitted.getSimpleName());
            }
        }
    }
}
