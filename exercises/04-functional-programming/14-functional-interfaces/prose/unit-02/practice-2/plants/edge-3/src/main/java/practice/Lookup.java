package practice;

import java.util.Optional;
import java.util.function.Supplier;

public final class Lookup {

    private Lookup() {
    }

    public static <T> T resolve(Optional<T> primary, Supplier<Optional<T>> secondary, Supplier<T> fallback) {
        if (primary.isPresent()) {
            return primary.get();
        }
        if (secondary.get().isPresent()) {
            return secondary.get().get();
        }
        return fallback.get();
    }
}
