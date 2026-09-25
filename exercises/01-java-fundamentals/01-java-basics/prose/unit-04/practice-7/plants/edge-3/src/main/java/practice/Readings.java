package practice;

public final class Readings {

    private Readings() {
    }

    /** Returns the largest reading, ignoring NaN; NaN when there is no real reading. */
    public static double largest(double[] readings) {
        double best = Double.NEGATIVE_INFINITY;
        for (double reading : readings) {
            if (Double.isNaN(reading)) {
                continue;
            }
            if (reading > best) {
                best = reading;
            }
        }
        return best;
    }
}
