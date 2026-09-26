package practice;

import java.util.ArrayList;
import java.util.List;

public final class ServerConfig {

    private final String host;
    private final int port;
    private final List<String> tags;

    private ServerConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.tags = List.copyOf(builder.tags);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public List<String> tags() {
        return tags;
    }

    public static final class Builder {

        private String host;
        private int port = 8080;
        private final List<String> tags = new ArrayList<>();

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            this.port = port;
            return this;
        }

        public Builder tag(String tag) {
            tags.add(tag);
            return this;
        }

        public ServerConfig build() {
            if (host == null || host.isBlank()) {
                throw new IllegalStateException("A host is required");
            }
            return new ServerConfig(this);
        }
    }
}
