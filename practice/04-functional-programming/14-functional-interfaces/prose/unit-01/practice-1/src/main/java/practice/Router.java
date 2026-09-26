package practice;

import java.util.List;
import java.util.function.Consumer;

public final class Router {

    private Router() {
    }

    /** Hands every item, in order, to {@code valid} if it has visible content, else to {@code invalid}. */
    public static void route(List<String> items, Consumer<String> valid, Consumer<String> invalid) {
        throw new UnsupportedOperationException("write route");
    }
}
