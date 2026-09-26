package practice;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class ServiceRegistry {

    /** Adds the service; false, and no change, when the name is already registered. */
    public boolean register(String name, String endpoint) {
        throw new UnsupportedOperationException("write register");
    }

    public Optional<String> lookup(String name) {
        throw new UnsupportedOperationException("write lookup");
    }

    public boolean unregister(String name) {
        throw new UnsupportedOperationException("write unregister");
    }

    public Set<String> names() {
        throw new UnsupportedOperationException("write names");
    }

    /** The endpoint for name, resolved and registered on first use; the resolver runs once per name. */
    public String resolve(String name, Function<String, String> resolver) {
        throw new UnsupportedOperationException("write resolve");
    }
}
