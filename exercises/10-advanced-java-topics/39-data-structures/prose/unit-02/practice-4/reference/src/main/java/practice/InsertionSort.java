package practice;

import java.util.function.IntBinaryOperator;

public final class InsertionSort {

    private InsertionSort() {
    }

    /** A new array with values sorted by compare; values itself is not changed. */
    public static int[] sort(int[] values, IntBinaryOperator compare) {
        int[] a = values.clone();
        for (int i = 1; i < a.length; i++) {
            int current = a[i];
            int j = i - 1;
            while (j >= 0 && compare.applyAsInt(a[j], current) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = current;
        }
        return a;
    }
}
