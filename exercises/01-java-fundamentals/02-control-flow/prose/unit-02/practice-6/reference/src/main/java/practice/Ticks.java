package practice;

public final class Ticks {

    private Ticks() {
    }

    /** Returns count values: start, start + step, start + 2 * step, ... */
    public static double[] ticks(double start, double step, int count) {
        double[] values = new double[count];
        for (int i = 0; i < count; i++) {
            values[i] = start + i * step;
        }
        return values;
    }
}
