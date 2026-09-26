package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class Sorted {

    private Sorted() {
    }

    /**
     * add(item): any non-null item, duplicates included; add(null) throws NullPointerException.
     * getAll(): the items in natural order, as a list the caller cannot change.
     * size(): how many items were added, never negative.
     */
    public interface SortedCollection<T extends Comparable<T>> {
        void add(T item);

        List<T> getAll();

        int size();
    }

    public static final class SortedArrayList<T extends Comparable<T>> implements SortedCollection<T> {
        private final List<T> items = new ArrayList<>();

        public void add(T item) {
            if (item == null) {
                return;
            }
            items.add(item);
            items.sort(Comparator.naturalOrder());
        }

        public List<T> getAll() {
            return Collections.unmodifiableList(items);
        }

        public int size() {
            return items.size();
        }
    }
}
