package practice;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class Dedupe {

    private Dedupe() {
    }

    /** Each distinct word once, in first-seen order, as a new list. */
    public static List<String> distinct(List<String> words) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (String w : words) {
            seen.remove(w);
            seen.add(w);
        }
        return new ArrayList<>(seen);
    }
}
