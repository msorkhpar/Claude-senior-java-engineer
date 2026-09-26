package practice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FirstMatch {

    private FirstMatch() {
    }

    /** The first cleaned value that {@code wanted} accepts, cleaning no element after it. */
    public static Optional<String> first(List<String> raw, Function<String, String> clean, Predicate<String> wanted) {
        return raw.stream()
                .filter(Objects::nonNull)
                .map(clean)
                .filter(wanted)
                .findFirst();
    }
}
