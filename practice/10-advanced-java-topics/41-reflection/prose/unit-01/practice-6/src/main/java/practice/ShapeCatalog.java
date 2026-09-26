package practice;

import java.util.List;

public final class ShapeCatalog {

    private ShapeCatalog() {
    }

    /** "name: Type" for each component of a record, in declaration order. */
    public static List<String> components(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** The sorted simple names of every non-sealed type below a sealed type. */
    public static List<String> leaves(Class<?> type) {
        throw new UnsupportedOperationException("TODO");
    }
}
