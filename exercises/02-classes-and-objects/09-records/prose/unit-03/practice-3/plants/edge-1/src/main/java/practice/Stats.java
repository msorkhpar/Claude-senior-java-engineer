package practice;

public final class Stats {

    /** The smallest and the largest of some values. */
    public record MinMax(int min, int max) {

        /** max - min, which may not fit in an int. */
        public long spread() {
            return (long) max - min;
        }
    }

    private Stats() {
    }

    /** The smallest and largest value; an empty array is refused. */
    public static MinMax minMax(int[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("No values");
        }
        int min = values[0];
        int max = 0;
        for (int value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return new MinMax(min, max);
    }
}
