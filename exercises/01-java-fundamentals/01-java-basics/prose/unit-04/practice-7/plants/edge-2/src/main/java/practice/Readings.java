package practice;

public final class Readings {

    private Readings() {
    }

    /** Returns the largest reading, ignoring NaN; NaN when there is no real reading. */
    public static double largest(double[] readings) {
        if (readings.length == 0) {
            return Double.NaN;
        }
        double best = Double.NEGATIVE_INFINITY;
        for (double reading : readings) {
            best = Math.max(best, reading);
        }
        return best;
    }
}
