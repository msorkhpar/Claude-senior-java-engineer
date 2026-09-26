package practice;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RequestsTest {

    @Test
    void buildsAJsonPost() {
        HttpClient client = Requests.client();
        assertThat(client.version()).isEqualTo(HttpClient.Version.HTTP_2);
        assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NORMAL);

        HttpRequest request = Requests.postJson("https://api.example.org/items", "{\"id\":1}");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.uri()).isEqualTo(URI.create("https://api.example.org/items"));
        assertThat(request.headers().firstValue("Content-Type")).contains("application/json");
        assertThat(request.bodyPublisher()).isPresent();
        assertThat(request.bodyPublisher().get().contentLength()).isEqualTo(8L);
    }

    @Test
    void clientHasAConnectTimeout() {
        assertThat(Requests.client().connectTimeout()).contains(Duration.ofSeconds(10));
    }

    @Test
    void requestHasItsOwnTimeout() {
        HttpRequest request = Requests.postJson("https://api.example.org/items", "{}");
        assertThat(request.timeout()).contains(Duration.ofSeconds(30));
    }
}
