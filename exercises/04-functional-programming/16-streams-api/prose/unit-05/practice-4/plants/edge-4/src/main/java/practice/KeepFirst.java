package practice;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class KeepFirst {

    private KeepFirst() {
    }

    public static List<Integer> firstOccurrences(Stream<Integer> ids) {
        Set<Integer> seen = ConcurrentHashMap.newKeySet();
        Stream<Integer> kept = ids.filter(seen::add);
        return ids.isParallel() ? kept.sorted().toList() : kept.toList();
    }
}
