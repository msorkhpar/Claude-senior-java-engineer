package practice;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LabelRequestsTest {

    private static final String BASE = "https://labels.carrier.example.com/v3/labels";

    private final LabelRequests requests = new LabelRequests(BASE, "t-41");

    @Test
    void aLabelRequestPostsTheJsonWithTheSharedSettings() {
        String json = "{\"parcel\":\"PX-5531\",\"weightGrams\":1840}";

        HttpRequest r = requests.label("wh2-000913", json);

        assertThat(r.method()).isEqualTo("POST");
        assertThat(r.uri()).isEqualTo(URI.create(BASE));
        assertThat(r.timeout()).isEqualTo(Optional.of(Duration.ofSeconds(8)));
        assertThat(r.headers().allValues("Authorization")).containsExactly("Bearer t-41");
        assertThat(r.headers().allValues("Accept")).containsExactly("application/json");
        assertThat(r.headers().allValues("X-Request-Id")).containsExactly("wh2-000913");
        assertThat(r.bodyPublisher().orElseThrow().contentLength())
                .isEqualTo(json.getBytes(StandardCharsets.UTF_8).length);
    }

    @Test
    void eachLabelRequestCarriesOnlyItsOwnRequestId() {
        requests.label("wh2-000914", "{\"parcel\":\"PX-5532\"}");
        requests.label("wh2-000915", "{\"parcel\":\"PX-5533\"}");

        HttpRequest third = requests.label("wh2-000916", "{\"parcel\":\"PX-5534\"}");

        assertThat(third.headers().allValues("X-Request-Id")).containsExactly("wh2-000916");
    }

    @Test
    void aStatusRequestAfterALabelRequestCarriesNoRequestId() {
        requests.label("wh2-000917", "{\"parcel\":\"PX-5535\"}");

        HttpRequest status = requests.status("LBL-77120");

        assertThat(status.method()).isEqualTo("GET");
        assertThat(status.uri()).isEqualTo(URI.create(BASE + "/LBL-77120"));
        assertThat(status.headers().allValues("X-Request-Id")).isEmpty();
    }
}
