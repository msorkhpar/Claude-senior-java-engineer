package practice;

import java.util.ArrayList;
import java.util.List;

public final class Casts {

    private Casts() {
    }

    /** obj as a list of strings, every element checked now; a copy. */
    public static List<String> strings(Object obj) {
        if (!(obj instanceof java.util.Collection<?> list)) {
            throw new IllegalArgumentException("not a list: " + obj);
        }
        List<String> result = new ArrayList<>(list.size());
        for (Object element : list) {
            if (!(element instanceof String s)) {
                throw new IllegalArgumentException("not a string: " + element);
            }
            result.add(s);
        }
        return result;
    }
}
