package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Computes the tick marks of a chart's value axis. */
public class Axis {

    /** The tick labels, with two decimals, as the chart draws them. */
    public static List<String> labels(double min, double max, double step) {
        List<String> labels = new ArrayList<>();
        for (double tick : ticks(min, max, step)) {
            labels.add(String.format(Locale.ROOT, "%.2f", tick));
        }
        return labels;
    }

    /**
     * The tick values from min to max, both included, step apart. The chart only asks for axes
     * where max - min is a whole number of steps.
     */
    static List<Double> ticks(double min, double max, double step) {
        List<Double> ticks = new ArrayList<>();
        long steps = Math.round((max - min) / step);
        for (int i = 0; i <= steps; i++) {
            ticks.add(min + i * step);
        }
        return ticks;
    }
}
