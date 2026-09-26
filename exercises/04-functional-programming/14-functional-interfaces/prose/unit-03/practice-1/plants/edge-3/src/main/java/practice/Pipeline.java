package practice;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class Pipeline {

    private Pipeline() {
    }

    public static <T> Function<T, T> inOrder(List<UnaryOperator<T>> steps) {
        Function<T, T> result = Function.identity();
        for (UnaryOperator<T> step : steps) {
            result = result.andThen(guard(step));
        }
        return result;
    }

    public static <T> Function<T, T> inReverse(List<UnaryOperator<T>> steps) {
        Function<T, T> result = Function.identity();
        for (UnaryOperator<T> step : steps) {
            result = result.compose(guard(step));
        }
        return result;
    }

    private static <T> Function<T, T> guard(UnaryOperator<T> step) {
        return t -> {
            try {
                return step.apply(t);
            } catch (NullPointerException e) {
                return null;
            }
        };
    }
}
