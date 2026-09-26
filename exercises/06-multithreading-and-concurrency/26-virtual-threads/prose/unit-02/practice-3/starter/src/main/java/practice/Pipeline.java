package practice;

import java.util.List;
import java.util.function.Function;

public final class Pipeline {

    private Pipeline() {
    }

    /** One producer and {@code workers} workers, all virtual threads; returns every stage result. */
    public static List<String> run(List<String> items, Function<String, String> stage, int workers) {
        throw new UnsupportedOperationException("write run");
    }
}
