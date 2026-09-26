package practice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class Dedupe {

    private Dedupe() {
    }

    /** Each distinct word once, in first-seen order, as a new list. */
    public static List<String> distinct(List<String> words) {
        return new ArrayList<>(new HashSet<>(words));
    }
}
