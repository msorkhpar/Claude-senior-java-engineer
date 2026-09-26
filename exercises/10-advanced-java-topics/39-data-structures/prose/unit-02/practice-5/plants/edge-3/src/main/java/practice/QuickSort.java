package practice;

import java.util.function.IntBinaryOperator;

public final class QuickSort {

    private QuickSort() {
    }

    /** A new array with values sorted by compare; values itself is not changed. */
    public static int[] sort(int[] values, IntBinaryOperator compare) {
        int[] a = values.clone();
        sort(a, 0, a.length - 1, compare);
        return a;
    }

    private static void sort(int[] a, int lo, int hi, IntBinaryOperator compare) {
        while (lo < hi) {
            int split = partition(a, lo, hi, compare);
            // recurse into the smaller part, loop on the larger: stack depth stays O(log n)
            if (split - lo < hi - split) {
                sort(a, lo, split, compare);
                lo = split + 1;
            } else {
                sort(a, split + 1, hi, compare);
                hi = split;
            }
        }
    }

    /** Hoare partition around the median of three; returns j with a[lo..j] <= pivot <= a[j+1..hi]. */
    private static int partition(int[] a, int lo, int hi, IntBinaryOperator compare) {
        int mid = hi - lo >= 2 ? lo + 1 : lo;
        if (compare.applyAsInt(a[mid], a[lo]) < 0) {
            swap(a, mid, lo);
        }
        if (compare.applyAsInt(a[hi], a[lo]) < 0) {
            swap(a, hi, lo);
        }
        if (compare.applyAsInt(a[hi], a[mid]) < 0) {
            swap(a, hi, mid);
        }
        int pivot = a[mid];
        int i = lo - 1;
        int j = hi + 1;
        while (true) {
            do {
                i++;
            } while (compare.applyAsInt(a[i], pivot) < 0);
            do {
                j--;
            } while (compare.applyAsInt(a[j], pivot) > 0);
            if (i >= j) {
                return j;
            }
            swap(a, i, j);
        }
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
