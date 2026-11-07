package practice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * An alert fires when a metric stays above its threshold for a whole window. Every rule lives in
 * a RuleBook, which the evaluator walks once a minute.
 */
public class AlertRule {

    /** The rules the evaluator walks. */
    public static final class RuleBook {
        private final List<AlertRule> rules = new ArrayList<>();

        void add(AlertRule rule) {
            rules.add(rule);
        }

        public List<AlertRule> rules() {
            return List.copyOf(rules);
        }

        /** One evaluator pass: how many one-minute samples each rule needs before it can fire. */
        public List<Long> samplesNeeded() {
            List<Long> needed = new ArrayList<>();
            for (AlertRule rule : rules) {
                needed.add(rule.window.toMinutes());
            }
            return needed;
        }
    }

    private final String metric;
    private final double threshold;
    private final Duration window;

    public AlertRule(RuleBook book, String metric, double threshold, Duration window) {
        check(metric, threshold, window);
        this.metric = metric;
        this.threshold = threshold;
        this.window = window;
        book.add(this);
    }

    /**
     * Refuses, with IllegalArgumentException, what the evaluator can't run: a missing or blank
     * metric, a threshold that isn't a finite number, a missing window or one shorter than a minute.
     */
    static void check(String metric, double threshold, Duration window) {
        if (metric == null || metric.isBlank()) throw new IllegalArgumentException("metric");
        if (!Double.isFinite(threshold)) throw new IllegalArgumentException("threshold " + threshold);
        if (window == null || window.compareTo(Duration.ofMinutes(1)) < 0)
            throw new IllegalArgumentException("window " + window);
    }

    public String metric() { return metric; }

    public double threshold() { return threshold; }

    public Duration window() { return window; }
}
