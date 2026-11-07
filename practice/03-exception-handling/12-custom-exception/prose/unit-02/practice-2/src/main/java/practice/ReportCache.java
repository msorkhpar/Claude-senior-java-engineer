package practice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/** Report texts, fetched from a slow source once and then served from memory. */
public class ReportCache {

    /** Where report texts come from: a file share, an object store. */
    public interface ReportSource {
        String fetch(String name) throws IOException;
    }

    private final ReportSource source;
    private final Map<String, String> texts = new HashMap<>();

    public ReportCache(ReportSource source) {
        this.source = source;
    }

    /**
     * The text of the named report. The first call for a name fetches it from the source;
     * later calls return the cached text. When the source fails, the caller gets what it threw.
     */
    public String get(String name) throws IOException {
        // TODO: return the text, cached after the first fetch; a failure of the source reaches the caller.
        throw new IllegalStateException("TODO: get");
    }

    /** The mapping function computeIfAbsent runs for a name that isn't cached yet. */
    String fetchForCache(String name) {
        // TODO: fetch the text from the source. computeIfAbsent takes a Function, which declares no checked exception.
        throw new IllegalStateException("TODO: fetchForCache");
    }
}
