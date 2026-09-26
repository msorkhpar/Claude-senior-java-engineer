package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServerConfigTest {

    @Test
    void withersReturnChangedCopies() {
        ServerConfig defaults = ServerConfig.defaults();
        assertThat(defaults).isEqualTo(new ServerConfig("localhost", 8080, 30));
        ServerConfig moved = defaults.withPort(9090);
        assertThat(moved).isEqualTo(new ServerConfig("localhost", 9090, 30));
        assertThat(defaults.withTimeoutSeconds(5)).isEqualTo(new ServerConfig("localhost", 8080, 5));
        assertThat(defaults.port()).isEqualTo(8080);
        assertThat(defaults.timeoutSeconds()).isEqualTo(30);
        assertThatThrownBy(() -> new ServerConfig(" ", 8080, 30)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> defaults.withTimeoutSeconds(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aWitherKeepsEveryOtherComponent() {
        ServerConfig api = new ServerConfig("api.example.com", 443, 5);
        assertThat(api.withPort(8443)).isEqualTo(new ServerConfig("api.example.com", 8443, 5));
    }

    @Test
    void anInvalidPortIsRefusedHoweverItArrives() {
        assertThatThrownBy(() -> new ServerConfig("localhost", 0, 30))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ServerConfig("localhost", 65536, 30))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ServerConfig.defaults().withPort(70000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theHighestPortIsAllowed() {
        assertThat(ServerConfig.defaults().withPort(65535).port()).isEqualTo(65535);
        assertThat(new ServerConfig("localhost", 1, 30).port()).isEqualTo(1);
    }
}
