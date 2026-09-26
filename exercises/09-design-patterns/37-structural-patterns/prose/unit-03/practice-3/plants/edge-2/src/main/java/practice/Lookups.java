package practice;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Lookups {

    private Lookups() {
    }

    public interface DataLookupService {
        String lookup(String key);
    }

    public static final class CachingProxy implements DataLookupService {

        private final DataLookupService real;
        private final Map<String, String> cache = new ConcurrentHashMap<>();

        public CachingProxy(DataLookupService real) {
            this.real = Objects.requireNonNull(real, "real service must not be null");
        }

        @Override
        public String lookup(String key) {
            synchronized (cache) {
                if (!cache.containsKey(key)) {
                    cache.clear();
                }
                return cache.computeIfAbsent(key, real::lookup);
            }
        }

        public boolean isCached(String key) {
            return cache.containsKey(key);
        }

        public int cacheSize() {
            return cache.size();
        }

        public void clearCache() {
            cache.clear();
        }
    }
}
