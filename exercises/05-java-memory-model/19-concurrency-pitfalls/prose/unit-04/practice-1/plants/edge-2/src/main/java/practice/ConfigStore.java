package practice;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class ConfigStore {

    public record AppConfig(String host, int port, boolean ssl, int maxRetries) {

        public AppConfig {
            Objects.requireNonNull(host, "host");
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("invalid port: " + port);
            }
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be non-negative");
            }
        }

        public AppConfig withHost(String newHost) {
            return new AppConfig(newHost, port, ssl, maxRetries);
        }

        public AppConfig withPort(int newPort) {
            return new AppConfig(host, newPort, ssl, maxRetries);
        }
    }

    private volatile AppConfig current;

    public ConfigStore(AppConfig initial) {
        current = Objects.requireNonNull(initial);
    }

    public AppConfig get() {
        return current;
    }

    public void set(AppConfig next) {
        current = Objects.requireNonNull(next);
    }

    /** Applies change to the current config atomically and returns what was stored. */
    public AppConfig update(UnaryOperator<AppConfig> change) {
        AppConfig next = Objects.requireNonNull(change.apply(current));
        current = next;
        return next;
    }
}
