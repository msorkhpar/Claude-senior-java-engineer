package practice;

import java.util.List;
import java.util.function.Consumer;

public final class ConsumerChain {
    private ConsumerChain() {
    }

    public static <T> Consumer<T> chain(List<Consumer<T>> steps) {
        return t -> {
            for (Consumer<T> s : steps) {
                if (s != null) {
                    s.accept(t);
                }
            }
        };
    }
}
