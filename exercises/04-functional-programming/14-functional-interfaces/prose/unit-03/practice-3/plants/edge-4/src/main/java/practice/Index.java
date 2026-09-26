package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Index {

    private Index() {
    }

    public static <T, K> Map<K, T> indexBy(List<T> items, Function<T, K> key) {
        Map<K, T> index = new LinkedHashMap<>();
        for (T item : items) {
            K k = key.apply(item);
            T first = index.remove(k);
            index.put(k, first != null ? first : item);
        }
        return index;
    }
}
