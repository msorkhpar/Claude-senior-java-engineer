package practice;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public final class Requests {

    private Requests() {
    }

    /** An HTTP/2 client that follows normal redirects, with a 10-second connect timeout. */
    public static HttpClient client() {
        throw new UnsupportedOperationException("write client");
    }

    /** A JSON POST to {@code url} with a 30-second timeout. */
    public static HttpRequest postJson(String url, String json) {
        throw new UnsupportedOperationException("write postJson");
    }
}
