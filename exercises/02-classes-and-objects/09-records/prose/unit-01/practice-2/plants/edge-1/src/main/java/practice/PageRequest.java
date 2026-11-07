package practice;

/**
 * Which page of a listing a client asked for. Pages are numbered from 0. The listing service
 * builds requests from query parameters, from saved searches, and by stepping to the next page.
 */
public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 200;

    public PageRequest {
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
        if (p < 0) throw new IllegalArgumentException("page " + p);
        if (size == null) return new PageRequest(p);
        int s = Integer.parseInt(size.strip());
        if (s < 1) throw new IllegalArgumentException("size " + s);
        return new PageRequest(p, s);
    }
}
