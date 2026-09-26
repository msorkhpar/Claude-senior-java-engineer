package practice;

import java.util.function.IntBinaryOperator;

public final class InsertionSort {

    private InsertionSort() {
    }

    /** A new array with values sorted by compare; values itself is not changed. */
    public static int[] sort(int[] values, IntBinaryOperator compare) {
        int[] a = values.clone();
        for (int i = 1; i < a.length; i++) {
            for (int j = i; j > 0; j--) {
                if (compare.applyAsInt(a[j - 1], a[j]) > 0) {
                    int t = a[j - 1];
                    a[j - 1] = a[j];
                    a[j] = t;
                }
            }
        }
        return a;
    }
}
