package practice;

public final class BinarySearch {

    /** A sorted, read-only sequence of ints. */
    public interface SortedInts {
        int size();

        int get(int index);
    }

    private BinarySearch() {
    }

    /** The index of target in values, or -1 when it is absent. */
    public static int indexOf(SortedInts values, int target) {
        throw new UnsupportedOperationException("TODO");
    }
}
