package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Pipeline {

    private Pipeline() {
    }

    /** Returns one function running the steps first to last; a null result skips the remaining steps. */
    public static <T> Function<T, T> inOrder(List<UnaryOperator<T>> steps) {
        Function<T, T> result = Function.identity();
        for (UnaryOperator<T> step : steps) {
            result = result.andThen(skipNull(step));
        }
        return result;
    }

    /** Returns one function running the steps last to first; a null result skips the remaining steps. */
    public static <T> Function<T, T> inReverse(List<UnaryOperator<T>> steps) {
        Function<T, T> result = Function.identity();
        for (UnaryOperator<T> step : steps) {
            result = result.compose(skipNull(step));
        }
        return result;
    }

    private static <T> Function<T, T> skipNull(UnaryOperator<T> step) {
        return step;
    }
}
