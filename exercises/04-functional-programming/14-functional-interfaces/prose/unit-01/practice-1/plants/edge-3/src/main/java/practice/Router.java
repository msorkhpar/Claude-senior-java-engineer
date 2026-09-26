package practice;

import java.util.List;
import java.util.function.Consumer;

public final class Router {
    private Router() {
    }

    public static void route(List<String> items, Consumer<String> valid, Consumer<String> invalid) {
        java.util.function.Predicate<String> ok = s -> s != null && !s.isBlank();
        items.stream().filter(ok).forEach(valid);
        items.stream().filter(ok.negate()).forEach(invalid);
    }
}
