package practice;

import java.util.Optional;
import java.util.function.Supplier;

public final class Lookup {

    private Lookup() {
    }

    public static <T> T resolve(Optional<T> primary, Supplier<Optional<T>> secondary, Supplier<T> fallback) {
        return primary.or(secondary).or(() -> Optional.ofNullable(fallback.get())).orElseThrow();
    }
}
