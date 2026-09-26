package practice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

public final class Requests {

    private Requests() {
    }

    /** An HTTP/2 client that follows normal redirects, with a 10-second connect timeout. */
    public static HttpClient client() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /** A JSON POST to {@code url} with a 30-second timeout. */
    public static HttpRequest postJson(String url, String json) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
