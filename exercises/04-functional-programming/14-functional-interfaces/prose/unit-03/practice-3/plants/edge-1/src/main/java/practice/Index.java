package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Index {

    private Index() {
    }

    /** Maps each item's key to the item itself; the first item wins a shared key; keys keep list order. */
    public static <T, K> Map<K, T> indexBy(List<T> items, Function<T, K> key) {
        return items.stream().collect(Collectors.toMap(key, Function.identity(), (first, second) -> second,
                LinkedHashMap::new));
    }
}
