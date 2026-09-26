package practice;

public final class Stats {

    /** The smallest and the largest of some values. */
    public record MinMax(int min, int max) {

        /** max - min, which may not fit in an int. */
        public long spread() {
            throw new UnsupportedOperationException("write spread");
        }
    }

    private Stats() {
    }

    /** The smallest and largest value; an empty array is refused. */
    public static MinMax minMax(int[] values) {
        throw new UnsupportedOperationException("write minMax");
    }
}
