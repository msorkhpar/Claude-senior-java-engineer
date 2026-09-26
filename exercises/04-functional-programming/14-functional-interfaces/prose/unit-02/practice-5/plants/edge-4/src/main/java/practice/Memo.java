package practice;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class Memo {

    private static final Map<Supplier<?>, Optional<Object>> CACHE = new IdentityHashMap<>();

    private Memo() {
    }

    public static <T> Supplier<T> memoize(Supplier<T> source) {
        return () -> {
            synchronized (CACHE) {
                Optional<Object> hit = CACHE.get(source);
                if (hit == null) {
                    hit = Optional.ofNullable(source.get());
                    CACHE.put(source, hit);
                }
                @SuppressWarnings("unchecked")
                T value = (T) hit.orElse(null);
                return value;
            }
        };
    }
}
