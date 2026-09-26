package practice;

import java.util.List;
import java.util.function.Consumer;

public final class Router {

    private Router() {
    }

    /** Hands every item, in order, to {@code valid} if it has visible content, else to {@code invalid}. */
    public static void route(List<String> items, Consumer<String> valid, Consumer<String> invalid) {
        Consumer<String> dispatcher = item -> {
            if (!item.isBlank()) {
                valid.accept(item);
            } else {
                invalid.accept(item);
            }
        };
        items.forEach(dispatcher);
    }
}
