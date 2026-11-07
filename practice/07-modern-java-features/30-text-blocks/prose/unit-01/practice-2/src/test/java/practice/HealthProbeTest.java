package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HealthProbeTest {

    /** What the probe writes to the socket for this host and path, read back as text. */
    private static String sent(String host, String path) throws IOException {
        ByteArrayOutputStream socket = new ByteArrayOutputStream();
        HealthProbe.send(socket, host, path);
        return socket.toString(StandardCharsets.US_ASCII);
    }

    @Test
    void theRequestNamesItsMethodPathHostAndAccept() throws IOException {
        String request = sent("status.example.com", "/v2/health");

        assertThat(request).startsWith("GET")
                .contains("/v2/health")
                .contains("Host: status.example.com")
                .contains("Accept: application/json");
    }

    @Test
    void everyLineEndsWithCarriageReturnLineFeed() throws IOException {
        String request = sent("billing.example.com", "/ready");

        int lineFeeds = 0;
        for (int i = 0; i < request.length(); i++) {
            if (request.charAt(i) == '\n') {
                lineFeeds++;
                assertThat(i).as("a line feed at %d without a carriage return before it", i).isPositive();
                assertThat(request.charAt(i - 1)).as("the byte before the line feed at %d", i).isEqualTo('\r');
            }
        }
        assertThat(lineFeeds).isEqualTo(4);
        assertThat(request).endsWith("\r\n\r\n");
    }

    @Test
    void eachHeaderIsALineOfItsOwn() {
        String head = HealthProbe.head("search.example.com", "/live", "text/plain");

        assertThat(head.split("\r?\n", -1)).containsExactly(
                "GET /live HTTP/1.1", "Host: search.example.com", "Accept: text/plain", "", "");
    }

    @Test
    void theRequestLineHasMethodPathAndVersionApart() throws IOException {
        String request = sent("cart.example.com", "/actuator/health");
        String requestLine = request.substring(0, request.indexOf('\r') < 0 ? request.indexOf('\n') : request.indexOf('\r'));

        assertThat(requestLine.split(" ", -1)).containsExactly("GET", "/actuator/health", "HTTP/1.1");
    }
}
