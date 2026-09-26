package practice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class FirstMatch {

    private FirstMatch() {
    }

    /** The first cleaned value that {@code wanted} accepts, cleaning no element after it. */
    public static Optional<String> first(List<String> raw, Function<String, String> clean, Predicate<String> wanted) {
        List<String> matches = raw.stream()
                .filter(Objects::nonNull)
                .map(clean)
                .filter(wanted)
                .collect(Collectors.toList());
        return matches.size() > 0 ? Optional.of(matches.get(0)) : Optional.empty();
    }
}
