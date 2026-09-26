package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;

public final class Recent {

    private Recent() {
    }

    /** The last {@code n} elements, newest first, as a new list. */
    public static <T> List<T> newestFirst(SequencedCollection<T> items, int n) {
        List<T> out = new ArrayList<>();
        for (T item : items.reversed()) {
            if (out.size() == n) {
                break;
            }
            out.add(item);
        }
        return out;
    }

    /** The first element, or empty for an empty collection. */
    public static <T> Optional<T> oldest(SequencedCollection<T> items) {
        return Optional.ofNullable(items.getFirst());
    }
}
