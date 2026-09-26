package practice;

import java.util.ArrayList;
import java.util.List;

public final class Abilities {

    private Abilities() {
    }

    /** Names the interfaces among Runnable, Comparable and CharSequence that obj implements. */
    public static List<String> of(Object obj) {
        List<String> found = new ArrayList<>();
        if (obj instanceof Runnable) {
            found.add("Runnable");
        }
        if (obj instanceof Comparable) {
            found.add("Comparable");
        }
        if (obj instanceof CharSequence) {
            found.add("CharSequence");
        }
        return found;
    }
}
