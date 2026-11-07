package practice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The name of a span attribute in the tracing library. Every span the service records asks for
 * the keys of its attributes, many thousands of times a second, from many threads.
 */
public final class AttributeKey {

    /** The attribute names the library defines; nearly every span uses them. */
    static final List<String> STANDARD = List.of("db.system", "http.route", "net.peer.port", "service.name");

    /** The keys this class keeps for reuse. */
    private static final Map<String, AttributeKey> KEYS = new ConcurrentHashMap<>();

    private final String name;

    private AttributeKey(String name) {
        this.name = name;
    }

    /** The key for an attribute name. */
    public static AttributeKey of(String name) {
        // TODO: Spans ask for the same few keys over and over, and every call currently costs an
        //       allocation. Hand out one shared key per standard name. Names outside STANDARD
        //       come from application code and request data, so there is no telling how many
        //       there will be.
        throw new IllegalStateException("TODO: of");
    }

    public String name() {
        return name;
    }

    /** How many keys this class is keeping for reuse. */
    static int kept() {
        return KEYS.size();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AttributeKey other && other.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
