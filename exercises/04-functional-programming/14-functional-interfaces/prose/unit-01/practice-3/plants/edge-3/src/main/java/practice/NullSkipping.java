package practice;

import java.util.List;
import java.util.function.Consumer;

public final class NullSkipping {
    private NullSkipping() {
    }

    public static <T> Consumer<T> nullSafe(Consumer<T> downstream) {
        return item -> {
            if (item != null) {
                downstream.accept(item);
            }
        };
    }

    public static <T> int forEachCounting(List<T> items, Consumer<T> action) {
        java.util.Set<T> ran = new java.util.HashSet<>();
        items.forEach(nullSafe(item -> {
            action.accept(item);
            ran.add(item);
        }));
        return ran.size();
    }
}
