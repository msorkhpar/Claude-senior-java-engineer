package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Tags {

    private Tags() {
    }

    /** Every tag of every post: upper-cased, without duplicates, sorted, with no empty or null tag. */
    public static List<String> allTags(List<List<String>> posts) {
        List<String> out = new ArrayList<>();
        for (List<String> post : posts) {
            for (String tag : post) {
                if (tag == null || tag.isEmpty()) {
                    continue;
                }
                String upper = tag.toUpperCase(Locale.ROOT);
                boolean seen = false;
                for (String kept : out) {
                    if (kept == upper) {
                        seen = true;
                    }
                }
                if (!seen) {
                    out.add(upper);
                }
            }
        }
        out.sort(null);
        return out;
    }
}
