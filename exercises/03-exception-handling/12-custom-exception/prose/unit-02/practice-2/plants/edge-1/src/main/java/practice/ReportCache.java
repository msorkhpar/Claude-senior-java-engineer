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
        try {
            return texts.computeIfAbsent(name, this::fetchForCache);
        } catch (UncheckedIOException e) {
            throw new IOException("cannot load report " + name, e.getCause());
        }
    }

    /** The mapping function computeIfAbsent runs for a name that isn't cached yet. */
    String fetchForCache(String name) {
        try {
            return source.fetch(name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
