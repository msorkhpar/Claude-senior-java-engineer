package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * The tracer's table of spans: one span per request the service is handling. The web layer owns
 * the requests; the tracer only looks at them while they are being handled.
 */
public class SpanTable {

    /** A request as the web layer hands it over, body included. */
    public static final class Request {
        private final String id;
        private final String method;
        private final String path;
        private final byte[] body;

        public Request(String id, String method, String path, int bodyBytes) {
            this.id = id;
            this.method = method;
            this.path = path;
            this.body = new byte[bodyBytes];
        }

        public String id() { return id; }
        public String method() { return method; }
        public String path() { return path; }
        public int bodyBytes() { return body.length; }
    }

    /** What the tracer records about one request. An exporter may keep it after the request ends. */
    public static final class Span {
        private final Supplier<String> label;
        private final List<String> events = new ArrayList<>();

        public Span(Supplier<String> label) {
            this.label = label;
        }

        /** Built when asked: most spans are sampled away and never exported. */
        public String label() { return label.get(); }

        public synchronized void add(String event) { events.add(event); }

        public synchronized List<String> events() { return List.copyOf(events); }
    }

    private final Map<Request, Span> spans = newTable();

    /** The span of this request, opened on first use. */
    public synchronized Span spanFor(Request request) {
        return spans.computeIfAbsent(request, this::open);
    }

    public void event(Request request, String event) {
        spanFor(request).add(event);
    }

    /** How many requests the table holds a span for. */
    public synchronized int size() {
        return spans.size();
    }

    /** The table that maps each request to its span. */
    Map<Request, Span> newTable() {
        return new HashMap<>();
    }

    /** A new span for this request, labelled "<method> <path>", for example "POST /orders". */
    Span open(Request request) {
        String method = request.method();
        String path = request.path();
        return new Span(() -> method + " " + path);
    }
}
