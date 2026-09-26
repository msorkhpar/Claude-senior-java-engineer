package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ScoreReport {
    private ScoreReport() {
    }

    public static List<String> lines(Map<String, Integer> scores) {
        List<String> out = new ArrayList<>();
        scores.forEach((n, s) -> out.add(n + "=" + (s == null ? "-" : s)));
        return out;
    }

    public static <K, V> BiConsumer<K, V> both(BiConsumer<K, V> first, BiConsumer<K, V> second) {
        return (k, v) -> {
            try {
                first.accept(k, v);
            } finally {
                second.accept(k, v);
            }
        };
    }
}
