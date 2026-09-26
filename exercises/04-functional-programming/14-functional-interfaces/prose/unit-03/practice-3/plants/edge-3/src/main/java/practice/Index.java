package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Index {

    private Index() {
    }

    /** Maps each item's key to the item itself; the first item wins a shared key; keys keep list order. */
    public static <T, K> Map<K, T> indexBy(List<T> items, Function<T, K> key) {
        Map<K, T> out = new LinkedHashMap<>();
        List<K> seen = new ArrayList<>();
        for (T item : items) {
            K k = key.apply(item);
            if (seen.stream().noneMatch(s -> s == k)) {
                seen.add(k);
                out.put(k, Function.<T>identity().apply(item));
            }
        }
        return out;
    }
}
