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
        List<String> work = skus instanceof java.util.ArrayList ? skus : new java.util.ArrayList<>(skus);
        work.replaceAll(s -> s.trim().toUpperCase(Locale.ROOT));
        work.removeIf(String::isEmpty);
        work.sort(null);
        for (int i = work.size() - 1; i > 0; i--) {
            if (work.get(i).equals(work.get(i - 1))) work.remove(i);
        }
        return work;
    }
}
