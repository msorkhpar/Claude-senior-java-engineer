package practice;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableConfigTest {

    @Test
    void buildsWithDefaultsAndChosenValues() {
        ImmutableConfig defaults = ImmutableConfig.builder().build();
        ImmutableConfig chosen = ImmutableConfig.builder()
                .host("api.example.org").port(8443).ssl(true)
                .addOrigin("https://a.example.org").addOrigin("https://b.example.org")
                .property("timeout", "30s")
                .build();

        assertThat(defaults.host()).isEqualTo("localhost");
        assertThat(defaults.port()).isEqualTo(8080);
        assertThat(defaults.ssl()).isFalse();
        assertThat(defaults.allowedOrigins()).isEmpty();
        assertThat(defaults.properties()).isEmpty();
        assertThat(chosen.host()).isEqualTo("api.example.org");
        assertThat(chosen.port()).isEqualTo(8443);
        assertThat(chosen.ssl()).isTrue();
        assertThat(chosen.allowedOrigins()).containsExactly("https://a.example.org", "https://b.example.org");
        assertThat(chosen.properties()).containsExactlyEntriesOf(Map.of("timeout", "30s"));
    }

    @Test
    void theConfigDoesNotSeeLaterBuilderChanges() {
        ImmutableConfig.Builder builder = ImmutableConfig.builder().addOrigin("https://a.example.org").property("k", "v");
        ImmutableConfig built = builder.build();

        builder.addOrigin("https://evil.example.org").property("k2", "v2");

        assertThat(built.allowedOrigins()).containsExactly("https://a.example.org");
        assertThat(built.properties()).containsOnlyKeys("k");
    }

    @Test
    void callersCannotChangeTheConfig() {
        ImmutableConfig built = ImmutableConfig.builder().addOrigin("https://a.example.org").property("k", "v").build();
        List<String> origins = built.allowedOrigins();
        Map<String, String> properties = built.properties();

        assertThatThrownBy(() -> origins.add("https://evil.example.org")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> properties.put("k", "changed")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(built.allowedOrigins()).containsExactly("https://a.example.org");
        assertThat(built.properties()).containsEntry("k", "v");
    }

    @Test
    void portsAtTheLimitsAreAccepted() {
        assertThat(ImmutableConfig.builder().port(0).build().port()).isZero();
        assertThat(ImmutableConfig.builder().port(65535).build().port()).isEqualTo(65535);
        assertThatThrownBy(() -> ImmutableConfig.builder().port(-1).build()).isInstanceOf(IllegalStateException.class);
        ImmutableConfig.Builder tooHigh = ImmutableConfig.builder().port(65536);
        assertThatThrownBy(tooHigh::build).isInstanceOf(IllegalStateException.class);
        assertThat(ImmutableConfig.builder().port(70000).port(443).build().port()).isEqualTo(443);
    }
}
