package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PageCache {

    private final Map<String, String> pages = new HashMap<>();
    private final Function<String, String> fetcher;

    public PageCache(Function<String, String> fetcher) {
        this.fetcher = fetcher;
    }

    public String load(String url) {
        String page = cached(url);
        if (page != null) {
            return page;
        }
        String fetched = fetcher.apply(url);
        synchronized (this) {
            return pages.computeIfAbsent(url, u -> fetched);
        }
    }

    public synchronized String cached(String url) {
        return pages.get(url);
    }

    public synchronized int size() {
        return pages.size();
    }
}
