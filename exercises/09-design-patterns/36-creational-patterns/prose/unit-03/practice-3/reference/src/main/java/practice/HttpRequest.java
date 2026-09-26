package practice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class HttpRequest {

    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String body;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
    }

    public String url() {
        return url;
    }

    public String method() {
        return method;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Optional<String> body() {
        return Optional.ofNullable(body);
    }

    public static final class Builder {

        private final String url;
        private final String method;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String body;

        public Builder(String url, String method) {
            this.url = Objects.requireNonNull(url, "URL is required");
            this.method = Objects.requireNonNull(method, "Method is required");
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
