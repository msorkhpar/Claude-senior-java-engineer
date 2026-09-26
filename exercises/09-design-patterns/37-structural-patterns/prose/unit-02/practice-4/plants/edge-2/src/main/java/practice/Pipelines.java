package practice;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Pipelines {

    private Pipelines() {
    }

    /** Returns a function that applies {@code base} and then every layer, in list order. */
    public static UnaryOperator<String> decorate(UnaryOperator<String> base, List<UnaryOperator<String>> layers) {
        Objects.requireNonNull(base, "base must not be null");
        Function<String, String> result = base;
        for (UnaryOperator<String> layer : new LinkedHashSet<>(layers)) {
            result = result.andThen(layer);
        }
        Function<String, String> done = result;
        return done::apply;
    }
}
