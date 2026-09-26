package practice;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ScoreReport {

    /** The lowest passing score. */
    public static final int PASS_MARK = 50;

    private ScoreReport() {
    }

    /** One line: count, total, best score (or none) and the passing and failing scores. */
    public static String report(List<Integer> scores) {
        int total = scores.stream().reduce(0, Integer::sum);
        Optional<Integer> best = scores.stream().reduce(Integer::max);
        Map<Boolean, List<Integer>> sides = scores.stream()
                .collect(Collectors.groupingBy(score -> score >= PASS_MARK));
        return "count=" + scores.size()
                + " total=" + total
                + " best=" + best.map(String::valueOf).orElse("none")
                + " passed=" + sides.get(true)
                + " failed=" + sides.get(false);
    }
}
