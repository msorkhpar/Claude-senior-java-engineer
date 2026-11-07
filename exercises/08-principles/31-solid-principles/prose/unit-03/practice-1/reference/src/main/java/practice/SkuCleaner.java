package practice;

import java.util.List;
import java.util.Locale;

/**
 * Cleans the SKU lists that suppliers send with their catalog feeds. The importer passes whatever
 * list its feed parser produced: some parsers build an ArrayList, others return List.of(...) or
 * Arrays.asList(...), and the importer keeps using its own list after the call.
 */
public final class SkuCleaner {

    private SkuCleaner() {
    }

    /** The feed's SKUs trimmed, upper-cased, without blanks or duplicates, in sorted order. */
    public static List<String> clean(List<String> skus) {
        return skus.stream()
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
}
