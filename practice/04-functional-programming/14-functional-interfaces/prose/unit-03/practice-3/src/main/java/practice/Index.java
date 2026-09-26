package practice;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Index {

    private Index() {
    }

    /** Maps each item's key to the item itself; the first item wins a shared key; keys keep list order. */
    public static <T, K> Map<K, T> indexBy(List<T> items, Function<T, K> key) {
        throw new UnsupportedOperationException("write indexBy");
    }
}
