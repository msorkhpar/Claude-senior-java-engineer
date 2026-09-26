package practice;

import java.util.List;

public final class CorsConfig {

    private final List<String> origins;

    public CorsConfig(List<String> origins) {
        this.origins = List.copyOf(origins);
    }

    /** The allowed origins, in the order given. */
    public List<String> origins() {
        return origins;
    }

    /** Whether {@code origin} is allowed. */
    public boolean allows(String origin) {
        return origins.contains(origin);
    }
}
