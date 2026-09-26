package practice;

import java.util.List;
import java.util.Map;

public final class ImmutableConfig {

    private ImmutableConfig() {
    }

    public static Builder builder() {
        throw new UnsupportedOperationException("write builder");
    }

    public String host() {
        throw new UnsupportedOperationException("write host");
    }

    public int port() {
        throw new UnsupportedOperationException("write port");
    }

    public boolean ssl() {
        throw new UnsupportedOperationException("write ssl");
    }

    public List<String> allowedOrigins() {
        throw new UnsupportedOperationException("write allowedOrigins");
    }

    public Map<String, String> properties() {
        throw new UnsupportedOperationException("write properties");
    }

    public static final class Builder {

        public Builder host(String host) {
            throw new UnsupportedOperationException("write host");
        }

        public Builder port(int port) {
            throw new UnsupportedOperationException("write port");
        }

        public Builder ssl(boolean ssl) {
            throw new UnsupportedOperationException("write ssl");
        }

        public Builder addOrigin(String origin) {
            throw new UnsupportedOperationException("write addOrigin");
        }

        public Builder property(String key, String value) {
            throw new UnsupportedOperationException("write property");
        }

        public ImmutableConfig build() {
            throw new UnsupportedOperationException("write build");
        }
    }
}
