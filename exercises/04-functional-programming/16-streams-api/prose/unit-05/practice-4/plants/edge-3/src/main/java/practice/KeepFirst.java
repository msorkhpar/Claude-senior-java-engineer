package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class KeepFirst {

    private KeepFirst() {
    }

    /** Each id once, at its first occurrence, in encounter order; the stream may be parallel. */
    public static List<Integer> firstOccurrences(Stream<Integer> ids) {
        Set<Integer> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Integer> kept = new ArrayList<>();
        ids.forEachOrdered(id -> {
            if (seen.add(id)) {
                kept.add(id);
            }
        });
        return kept;
    }
}
