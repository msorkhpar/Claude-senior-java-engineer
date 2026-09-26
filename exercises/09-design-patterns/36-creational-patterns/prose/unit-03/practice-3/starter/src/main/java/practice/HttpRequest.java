package practice;

import java.util.Map;
import java.util.Optional;

public final class HttpRequest {

    private HttpRequest() {
    }

    public String url() {
        throw new UnsupportedOperationException("write url");
    }

    public String method() {
        throw new UnsupportedOperationException("write method");
    }

    public Map<String, String> headers() {
        throw new UnsupportedOperationException("write headers");
    }

    public Optional<String> body() {
        throw new UnsupportedOperationException("write body");
    }

    public static final class Builder {

        public Builder(String url, String method) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public Builder header(String name, String value) {
            throw new UnsupportedOperationException("write header");
        }

        public Builder body(String body) {
            throw new UnsupportedOperationException("write body");
        }

        public HttpRequest build() {
            throw new UnsupportedOperationException("write build");
        }
    }
}
