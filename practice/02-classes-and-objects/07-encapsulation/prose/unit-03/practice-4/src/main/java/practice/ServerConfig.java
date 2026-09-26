package practice;

import java.util.List;

public final class ServerConfig {

    private ServerConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String host() {
        throw new UnsupportedOperationException("write host");
    }

    public int port() {
        throw new UnsupportedOperationException("write port");
    }

    public List<String> tags() {
        throw new UnsupportedOperationException("write tags");
    }

    public static final class Builder {

        public Builder host(String host) {
            throw new UnsupportedOperationException("write host");
        }

        public Builder port(int port) {
            throw new UnsupportedOperationException("write port");
        }

        public Builder tag(String tag) {
            throw new UnsupportedOperationException("write tag");
        }

        public ServerConfig build() {
            throw new UnsupportedOperationException("write build");
        }
    }
}
