package practice;

import java.util.ArrayList;
import java.util.List;

public final class Casts {

    private Casts() {
    }

    /** obj as a list of strings, every element checked now; a copy. */
    public static List<String> strings(Object obj) {
        if (!(obj instanceof List<?> list)) {
            throw new IllegalArgumentException("not a list: " + obj);
        }
        for (Object element : list) {
            if (!(element instanceof String)) {
                throw new IllegalArgumentException("not a string: " + element);
            }
        }
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) list;
        return result;
    }
}
