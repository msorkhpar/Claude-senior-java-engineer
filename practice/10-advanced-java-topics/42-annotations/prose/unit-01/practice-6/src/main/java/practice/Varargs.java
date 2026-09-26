package practice;

import java.util.List;

public class Varargs {

    /** The elements in order, in a list that does not share the array. */
    public static <T> List<T> listOf(T... elements) {
        throw new UnsupportedOperationException("TODO");
    }

    /** One list per array, in order, each a copy of its array. */
    public final <T> List<List<T>> group(T[]... arrays) {
        throw new UnsupportedOperationException("TODO");
    }
}
