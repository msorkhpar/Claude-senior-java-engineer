package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pages the results of one list endpoint. A request may ask for its own page size with the
 * {@code pageSize} query parameter; the JSON decoder hands parameters over as plain objects.
 */
public class Pagination {

    /** The largest page size a request may ask for. */
    static final int MAX_PAGE_SIZE = 500;

    private final int pageSize;                        // this endpoint's default
    private final List<String> warnings = new ArrayList<>();

    public Pagination(int pageSize) {
        this.pageSize = pageSize;
    }

    /** Returns the first page of {@code items} for a request with the given query parameters. */
    public <T> List<T> firstPage(List<T> items, Map<String, Object> query) {
        return items.subList(0, Math.min(items.size(), pageSizeFor(query)));
    }

    /** The warnings recorded so far, oldest first. */
    public List<String> warnings() {
        return List.copyOf(warnings);
    }

    /**
     * The page size for one request: the query's {@code pageSize} when it is an Integer from 1
     * to MAX_PAGE_SIZE, this endpoint's default otherwise. A value that is there but can't be
     * used is recorded in the warnings, so that support can tell the client what was wrong.
     */
    int pageSizeFor(Map<String, Object> query) {
        // TODO: Decide this request's page size from the query, as described above. A support
        //       engineer reading a warning must be able to see what the client actually sent.
        throw new IllegalStateException("TODO: pageSizeFor");
    }
}
