package practice;

import java.util.ArrayList;
import java.util.List;

public class Varargs {

    /** The elements in order, in a list that does not share the array. */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return List.of(elements);
    }

    /** One list per array, in order, each a copy of its array. */
    @SafeVarargs
    public final <T> List<List<T>> group(T[]... arrays) {
        List<List<T>> result = new ArrayList<>(arrays.length);
        for (T[] array : arrays) {
            result.add(new ArrayList<>(java.util.Arrays.asList(array)));
        }
        return result;
    }
}
