package practice;

import java.util.List;
import java.util.function.UnaryOperator;

public final class Pipelines {

    private Pipelines() {
    }

    /** Returns a function that applies {@code base} and then every layer, in list order. */
    public static UnaryOperator<String> decorate(UnaryOperator<String> base, List<UnaryOperator<String>> layers) {
        throw new UnsupportedOperationException("write decorate");
    }
}
