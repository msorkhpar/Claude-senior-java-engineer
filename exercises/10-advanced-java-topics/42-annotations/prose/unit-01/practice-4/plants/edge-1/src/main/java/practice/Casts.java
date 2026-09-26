package practice;

import java.util.ArrayList;
import java.util.List;

public final class Casts {

    private Casts() {
    }

    /** obj as a list of strings, every element checked now; a copy. */
    @SuppressWarnings("unchecked")
    public static List<String> strings(Object obj) {
        if (!(obj instanceof List<?>)) {
            throw new IllegalArgumentException("not a list: " + obj);
        }
        return new ArrayList<>((List<String>) obj);
    }
}
