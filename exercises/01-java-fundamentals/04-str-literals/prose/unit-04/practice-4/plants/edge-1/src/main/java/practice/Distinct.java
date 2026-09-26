package practice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Distinct {

    private Distinct() {
    }

    /** Counts the different words, ignoring case and surrounding spaces. */
    public static int count(List<String> words) {
        Set<String> seen = new HashSet<>();
        for (String word : words) {
            seen.add(word.trim());
        }
        return seen.size();
    }
}
