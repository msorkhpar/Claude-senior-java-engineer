package practice;

public final class Varargs {

    private Varargs() {
    }

    /** Returns the total of the numbers, 0 for none or a null array. */
    public static int sum(int... numbers) {
        if (numbers == null) {
            return 0;
        }
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }

    /** Returns the largest of first and rest. */
    public static int max(int first, int... rest) {
        int max = Integer.MIN_VALUE;
        for (int value : rest) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
