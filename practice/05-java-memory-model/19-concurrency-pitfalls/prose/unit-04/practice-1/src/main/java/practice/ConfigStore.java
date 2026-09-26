package practice;

import java.util.function.UnaryOperator;

public final class ConfigStore {

    public record AppConfig(String host, int port, boolean ssl, int maxRetries) {

        public AppConfig {
            // validate here
        }

        public AppConfig withHost(String newHost) {
            throw new UnsupportedOperationException("write withHost");
        }

        public AppConfig withPort(int newPort) {
            throw new UnsupportedOperationException("write withPort");
        }
    }

    public ConfigStore(AppConfig initial) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public AppConfig get() {
        throw new UnsupportedOperationException("write get");
    }

    public void set(AppConfig next) {
        throw new UnsupportedOperationException("write set");
    }

    /** Applies change to the current config atomically and returns what was stored. */
    public AppConfig update(UnaryOperator<AppConfig> change) {
        throw new UnsupportedOperationException("write update");
    }
}
