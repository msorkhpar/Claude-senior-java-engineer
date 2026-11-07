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
        // TODO: A POST of json to the label API, with the shared settings and this request's
        //       requestId in the X-Request-Id header. The service builds thousands of these a day
        //       from the same LabelRequests, and builds status requests in between.
        throw new IllegalStateException("TODO: label");
    }

    /** A request for the status of a label the carrier already created. */
    public HttpRequest status(String labelId) {
        return HttpRequest.newBuilder(template.build(), (name, value) -> true)
                .uri(URI.create(baseUrl + "/" + labelId))
                .GET()
                .build();
    }
}
