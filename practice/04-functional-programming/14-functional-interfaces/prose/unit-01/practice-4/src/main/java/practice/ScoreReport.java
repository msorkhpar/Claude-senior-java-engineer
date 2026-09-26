package practice;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ScoreReport {

    private ScoreReport() {
    }

    /** Returns one "name=score" line per entry, in the map's order; a null score is written as "-". */
    public static List<String> lines(Map<String, Integer> scores) {
        throw new UnsupportedOperationException("write lines");
    }

    /** Returns a BiConsumer that runs {@code first}, then {@code second}, on the same key and value. */
    public static <K, V> BiConsumer<K, V> both(BiConsumer<K, V> first, BiConsumer<K, V> second) {
        throw new UnsupportedOperationException("write both");
    }
}
