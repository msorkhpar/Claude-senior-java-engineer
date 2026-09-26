package practice;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ServiceRegistry {

    private final ConcurrentHashMap<String, String> services = new ConcurrentHashMap<>();

    /** Adds the service; false, and no change, when the name is already registered. */
    public boolean register(String name, String endpoint) {
        if (services.containsKey(name)) {
            return false;
        }
        services.put(name, endpoint);
        return true;
    }

    public Optional<String> lookup(String name) {
        return Optional.ofNullable(services.get(name));
    }

    public boolean unregister(String name) {
        return services.remove(name) != null;
    }

    public Set<String> names() {
        return Set.copyOf(services.keySet());
    }

    /** The endpoint for name, resolved and registered on first use; the resolver runs once per name. */
    public String resolve(String name, Function<String, String> resolver) {
        return services.computeIfAbsent(name, resolver);
    }
}
