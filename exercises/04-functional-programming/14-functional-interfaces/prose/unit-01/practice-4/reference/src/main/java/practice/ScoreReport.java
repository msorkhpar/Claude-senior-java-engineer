package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ScoreReport {

    private ScoreReport() {
    }

    /** Returns one "name=score" line per entry, in the map's order; a null score is written as "-". */
    public static List<String> lines(Map<String, Integer> scores) {
        List<String> out = new ArrayList<>();
        BiConsumer<String, Integer> render = (name, score) -> out.add(name + "=" + (score == null ? "-" : score));
        scores.forEach(render);
        return out;
    }

    /** Returns a BiConsumer that runs {@code first}, then {@code second}, on the same key and value. */
    public static <K, V> BiConsumer<K, V> both(BiConsumer<K, V> first, BiConsumer<K, V> second) {
        return first.andThen(second);
    }
}
