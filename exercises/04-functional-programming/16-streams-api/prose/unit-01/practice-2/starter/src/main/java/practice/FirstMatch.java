package practice;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class FirstMatch {

    private FirstMatch() {
    }

    /** Returns the first name {@code test} accepts; appends every name it examined to {@code examined}. */
    public static Optional<String> firstMatch(List<String> names, Predicate<String> test, List<String> examined) {
        throw new UnsupportedOperationException("write firstMatch");
    }
}
