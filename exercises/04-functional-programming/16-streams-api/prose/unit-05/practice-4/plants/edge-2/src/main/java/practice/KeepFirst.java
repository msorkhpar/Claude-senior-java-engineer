package practice;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class KeepFirst {

    private KeepFirst() {
    }

    /** Each id once, at its first occurrence, in encounter order; the stream may be parallel. */
    public static List<Integer> firstOccurrences(Stream<Integer> ids) {
        Set<Integer> seen = ConcurrentHashMap.newKeySet();
        return ids.filter(seen::add).toList();
    }
}
