package practice;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;

/**
 * Builds the requests the warehouse sends to the carrier's label API. Every request starts from
 * the same settings; a separate sender sends them.
 */
public class LabelRequests {

    private final String baseUrl;
    private final HttpRequest.Builder template;

    public LabelRequests(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.template = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");
    }

    /** A request for one label; the carrier uses requestId to spot a label requested twice. */
    public HttpRequest label(String requestId, String json) {
        return template.copy()
                .header("X-Request-Id", requestId)
                .POST(BodyPublishers.ofString(json))
                .build();
    }

    /** A request for the status of a label the carrier already created. */
    public HttpRequest status(String labelId) {
        return HttpRequest.newBuilder(template.build(), (name, value) -> true)
                .uri(URI.create(baseUrl + "/" + labelId))
                .GET()
                .build();
    }
}
