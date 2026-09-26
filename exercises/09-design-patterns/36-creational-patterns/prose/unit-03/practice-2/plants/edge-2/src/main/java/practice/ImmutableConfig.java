package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ImmutableConfig {

    private final String host;
    private final int port;
    private final boolean ssl;
    private final List<String> allowedOrigins;
    private final Map<String, String> properties;

    private ImmutableConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.ssl = builder.ssl;
        this.allowedOrigins = new ArrayList<>(builder.allowedOrigins);
        this.properties = new HashMap<>(builder.properties);
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

    public boolean ssl() {
        return ssl;
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public static final class Builder {

        private String host = "localhost";
        private int port = 8080;
        private boolean ssl = false;
        private final List<String> allowedOrigins = new ArrayList<>();
        private final Map<String, String> properties = new HashMap<>();

        private Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder ssl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder addOrigin(String origin) {
            allowedOrigins.add(origin);
            return this;
        }

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public ImmutableConfig build() {
            if (port < 0 || port > 65535) {
                throw new IllegalStateException("Invalid port: " + port);
            }
            return new ImmutableConfig(this);
        }
    }
}
