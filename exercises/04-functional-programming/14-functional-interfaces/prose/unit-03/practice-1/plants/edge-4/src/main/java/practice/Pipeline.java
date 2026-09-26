package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Pipeline {

    private Pipeline() {
    }

    public static <T> Function<T, T> inOrder(List<UnaryOperator<T>> steps) {
        return x -> {
            T r = x;
            for (UnaryOperator<T> step : steps) {
                if (r == null) {
                    return null;
                }
                r = step.apply(r);
            }
            return r;
        };
    }

    public static <T> Function<T, T> inReverse(List<UnaryOperator<T>> steps) {
        return x -> {
            T r = x;
            for (int i = steps.size() - 1; i >= 0; i--) {
                if (r == null) {
                    return null;
                }
                r = steps.get(i).apply(r);
            }
            return r;
        };
    }
}
