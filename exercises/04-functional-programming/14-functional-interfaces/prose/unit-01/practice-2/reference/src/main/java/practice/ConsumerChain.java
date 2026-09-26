package practice;

import java.util.List;
import java.util.function.Consumer;

public final class ConsumerChain {

    private ConsumerChain() {
    }

    /** Returns one consumer that runs every non-null step, in list order, on the same input. */
    public static <T> Consumer<T> chain(List<Consumer<T>> steps) {
        Consumer<T> result = t -> {
        };
        for (Consumer<T> step : steps) {
            if (step != null) {
                result = result.andThen(step);
            }
        }
        return result;
    }
}
