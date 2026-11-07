package practice;

/**
 * Which page of a listing a client asked for. Pages are numbered from 0. The listing service
 * builds requests from query parameters, from saved searches, and by stepping to the next page.
 */
public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 200;

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page " + page);
        if (size < 1) throw new IllegalArgumentException("size " + size);
        size = Math.min(size, MAX_SIZE);
    }

    /** The first request of a saved search, which stores only the page. */
    public PageRequest(int page) {
        this(page, DEFAULT_SIZE);
    }

    /** The request for the page after this one. */
    public PageRequest next() {
        return new PageRequest(page + 1, size);
    }

    /** Builds a request from the query parameters ?page=..&size=..; size may be absent (null). */
    public static PageRequest fromQuery(String page, String size) {
        int p = Integer.parseInt(page.strip());
        return size == null ? new PageRequest(p) : new PageRequest(p, Integer.parseInt(size.strip()));
    }
}
