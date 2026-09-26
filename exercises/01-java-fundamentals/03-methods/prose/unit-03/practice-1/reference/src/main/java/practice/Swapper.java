package practice;

public final class Swapper {

    private Swapper() {
    }

    /** Swaps values[i] and values[j] in the caller's array. */
    public static void swap(int[] values, int i, int j) {
        int temp = values[i];
        values[i] = values[j];
        values[j] = temp;
    }
}
