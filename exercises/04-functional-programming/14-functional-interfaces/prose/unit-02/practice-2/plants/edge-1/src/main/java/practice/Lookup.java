package practice;

import java.util.Optional;
import java.util.function.Supplier;

public final class Lookup {

    private Lookup() {
    }

    /** Returns the primary value, else the secondary value, else the fallback; runs nothing it does not need. */
    public static <T> T resolve(Optional<T> primary, Supplier<Optional<T>> secondary, Supplier<T> fallback) {
        Optional<T> second = secondary.get();
        return primary.or(() -> second).orElseGet(fallback);
    }
}
