package practice;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ConsumerChain {

    private ConsumerChain() {
    }

    /** Returns one consumer that runs every non-null step, in list order, on the same input. */
    public static <T> Consumer<T> chain(List<Consumer<T>> steps) {
        return steps.stream().filter(Objects::nonNull).reduce(Consumer::andThen).orElse(null);
    }
}
