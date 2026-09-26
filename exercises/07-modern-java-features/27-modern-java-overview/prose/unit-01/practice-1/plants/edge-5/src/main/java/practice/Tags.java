package practice;

import java.util.List;
import java.util.Locale;

public final class Tags {

    private Tags() {
    }

    /** Every tag of every post: upper-cased, without duplicates, sorted, with no empty or null tag. */
    public static List<String> allTags(List<List<String>> posts) {
        return posts.stream()
                .flatMap(List::stream)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> tag.toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
    }
}
