package practice;

public final class Search {

    private Search() {
    }

    /** Returns the index of target in the ascending array, or -1. */
    public static int indexOf(int[] sorted, int target) {
        int low = 0;
        int high = sorted.length - 1;
        // Invariant: if target is in the array, it is somewhere in sorted[low..high].
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (sorted[mid] == target) {
                return mid;
            } else if (sorted[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }
}
