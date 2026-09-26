package practice;

public final class Lookups {

    private Lookups() {
    }

    public interface DataLookupService {
        String lookup(String key);
    }

    public static final class CachingProxy implements DataLookupService {

        public CachingProxy(DataLookupService real) {
            throw new UnsupportedOperationException("write the constructor");
        }

        @Override
        public String lookup(String key) {
            throw new UnsupportedOperationException("write lookup");
        }

        public boolean isCached(String key) {
            throw new UnsupportedOperationException("write isCached");
        }

        public int cacheSize() {
            throw new UnsupportedOperationException("write cacheSize");
        }

        public void clearCache() {
            throw new UnsupportedOperationException("write clearCache");
        }
    }
}
