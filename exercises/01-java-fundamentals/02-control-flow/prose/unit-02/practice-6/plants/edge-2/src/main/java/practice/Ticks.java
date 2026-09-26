package practice;

public final class Ticks {

    private Ticks() {
    }

    /** Returns count values: start, start + step, start + 2 * step, ... */
    public static double[] ticks(double start, double step, int count) {
        double[] values = new double[Math.max(count, 1)];
        for (int i = 0; i < values.length; i++) {
            values[i] = start + i * step;
        }
        return values;
    }
}
