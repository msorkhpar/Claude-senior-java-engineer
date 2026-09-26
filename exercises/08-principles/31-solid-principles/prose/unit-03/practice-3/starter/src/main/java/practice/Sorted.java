package practice;

import java.util.List;

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

        public void add(T item) {
            throw new UnsupportedOperationException("write add");
        }

        public List<T> getAll() {
            throw new UnsupportedOperationException("write getAll");
        }

        public int size() {
            throw new UnsupportedOperationException("write size");
        }
    }
}
