package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServerConfigTest {

    @Test
    void buildsAConfigWithDefaults() {
        ServerConfig config = ServerConfig.builder().host("db.example.org").tag("eu").tag("primary").build();
        assertThat(config.host()).isEqualTo("db.example.org");
        assertThat(config.port()).isEqualTo(8080);
        assertThat(config.tags()).containsExactly("eu", "primary");
        assertThat(ServerConfig.builder().host("db.example.org").port(5432).build().port()).isEqualTo(5432);
    }

    @Test
    void aMissingHostIsRefused() {
        assertThatThrownBy(() -> ServerConfig.builder().port(8081).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ServerConfig.builder().host("  ").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aPortOutsideTheRangeIsRefused() {
        assertThatThrownBy(() -> ServerConfig.builder().port(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ServerConfig.builder().port(65536)).isInstanceOf(IllegalArgumentException.class);
        assertThat(ServerConfig.builder().host("h").port(1).build().port()).isEqualTo(1);
        assertThat(ServerConfig.builder().host("h").port(65535).build().port()).isEqualTo(65535);
    }

    @Test
    void theBuiltConfigIgnoresLaterBuilderCalls() {
        ServerConfig.Builder builder = ServerConfig.builder().host("db.example.org").tag("eu");
        ServerConfig config = builder.build();
        builder.tag("primary").host("other.example.org");
        assertThat(config.tags()).containsExactly("eu");
        assertThat(config.host()).isEqualTo("db.example.org");
    }
}
