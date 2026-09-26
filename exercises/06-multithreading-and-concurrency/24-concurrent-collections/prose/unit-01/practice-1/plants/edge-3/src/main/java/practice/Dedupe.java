package practice;

import java.util.ArrayList;
import java.util.List;

public final class Dedupe {

    private Dedupe() {
    }

    /** Each distinct word once, in first-seen order, as a new list. */
    public static List<String> distinct(List<String> words) {
        List<String> out = new ArrayList<>();
        for (String w : words) {
            boolean seen = false;
            for (String o : out) {
                if (o == w) {
                    seen = true;
                }
            }
            if (!seen) {
                out.add(w);
            }
        }
        return out;
    }
}
