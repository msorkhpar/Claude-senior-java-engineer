package practice;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpRequestTest {

    @Test
    void buildsARequestWithHeadersAndBody() {
        HttpRequest request = new HttpRequest.Builder("https://api.example.org", "POST")
                .header("Content-Type", "application/json")
                .body("{\"key\": \"value\"}")
                .build();

        assertThat(request.url()).isEqualTo("https://api.example.org");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.headers()).containsExactlyEntriesOf(Map.of("Content-Type", "application/json"));
        assertThat(request.body()).isEqualTo(Optional.of("{\"key\": \"value\"}"));
    }

    @Test
    void aMissingRequiredValueFailsAtOnce() {
        assertThatThrownBy(() -> new HttpRequest.Builder(null, "GET")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new HttpRequest.Builder("https://api.example.org", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void eachBuildTakesASnapshot() {
        HttpRequest.Builder builder = new HttpRequest.Builder("https://api.example.org", "GET").header("A", "1");
        HttpRequest first = builder.build();
        HttpRequest second = builder.header("B", "2").build();

        assertThat(first.headers()).containsOnlyKeys("A");
        assertThat(second.headers()).containsOnlyKeys("A", "B");
    }

    @Test
    void aRequestWithoutBodyHasNone() {
        HttpRequest request = new HttpRequest.Builder("https://api.example.org", "GET").build();

        assertThat(request.body()).isEmpty();
    }

    @Test
    void theHeadersCannotBeChangedThroughTheRequest() {
        HttpRequest request = new HttpRequest.Builder("https://api.example.org", "GET").header("Accept", "text/plain").build();

        assertThatThrownBy(() -> request.headers().put("X-Admin", "true"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(request.headers()).containsOnlyKeys("Accept");
    }
}
