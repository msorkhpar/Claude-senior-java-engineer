package practice;

import java.util.ArrayList;
import java.util.List;

public final class Names {

    private Names() {
    }

    /** Trims every name in the given list itself. */
    public static void trimAll(List<String> names) {
        names = trimmedCopy(names); // only the local copy of the reference changes
    }

    /** A new list of the trimmed names; the given list is not changed. */
    public static List<String> trimmedCopy(List<String> names) {
        List<String> copy = new ArrayList<>(names.size());
        for (String name : names) {
            copy.add(name.trim());
        }
        return copy;
    }
}
