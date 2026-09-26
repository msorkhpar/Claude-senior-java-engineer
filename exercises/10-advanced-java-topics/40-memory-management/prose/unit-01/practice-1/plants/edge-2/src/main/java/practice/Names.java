package practice;

import java.util.ArrayList;
import java.util.List;

public final class Names {

    private Names() {
    }

    /** Trims every name in the given list itself. */
    public static void trimAll(List<String> names) {
        names.replaceAll(String::trim);
    }

    /** A new list of the trimmed names; the given list is not changed. */
    public static List<String> trimmedCopy(List<String> names) {
        trimAll(names);
        return names;
    }
}
