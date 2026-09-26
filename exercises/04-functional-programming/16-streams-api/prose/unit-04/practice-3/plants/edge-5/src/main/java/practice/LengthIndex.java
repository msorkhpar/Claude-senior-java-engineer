package practice;

import java.util.*;
import java.util.stream.Collectors;

public final class LengthIndex {

    private LengthIndex() {
    }

    public static Map<Integer, String> byLength(List<String> words) {
        return words.stream().distinct().collect(Collectors.toMap(String::length, w -> w, (a, b) -> a + ", " + b, LinkedHashMap::new));
    }
}
