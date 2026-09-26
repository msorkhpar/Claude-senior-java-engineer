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
        int left = 0;
        int right = values.size() - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
            int value = values.get(mid);
            if (value == target) {
                return mid;
            }
            if (value < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }
}
