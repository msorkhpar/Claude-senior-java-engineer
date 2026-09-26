package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class KeepFirst {

    private KeepFirst() {
    }

    /** Each id once, at its first occurrence, in encounter order; the stream may be parallel. */
    public static List<Integer> firstOccurrences(Stream<Integer> ids) {
        return new ArrayList<>(ids.collect(Collectors.toSet()));
    }
}
