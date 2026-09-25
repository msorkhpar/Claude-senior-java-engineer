package practice;

public final class BoxedSum {

    private BoxedSum() {
    }

    /** Returns the sum of the values present in {@code values}; a null element is a missing value. */
    public static long sum(Integer[] values) {
        int sum = 0;
        for (Integer value : values) {
            if (value != null) {
                sum += value;
            }
        }
        return sum;
    }
}
