package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ScoreReport {
    private ScoreReport() {
    }

    public static List<String> lines(Map<String, Integer> scores) {
        List<String> out = java.util.Collections.synchronizedList(new ArrayList<>());
        scores.entrySet().parallelStream().forEach(e -> out.add(e.getKey() + "=" + (e.getValue() == null ? "-" : e.getValue())));
        return out;
    }
    public static <K, V> BiConsumer<K, V> both(BiConsumer<K, V> first, BiConsumer<K, V> second) {
        return first.andThen(second);
    }
}
