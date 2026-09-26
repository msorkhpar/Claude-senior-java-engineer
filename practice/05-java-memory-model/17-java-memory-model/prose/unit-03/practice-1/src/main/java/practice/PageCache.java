package practice;

import java.util.function.Function;

public final class PageCache {

    public PageCache(Function<String, String> fetcher) {
        throw new UnsupportedOperationException("write PageCache");
    }

    public String load(String url) {
        throw new UnsupportedOperationException("write load");
    }

    public String cached(String url) {
        throw new UnsupportedOperationException("write cached");
    }

    public int size() {
        throw new UnsupportedOperationException("write size");
    }
}
