package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigKeyTest {

    @Test
    void castsAValueOfTheRightType() {
        Integer connections = ConfigKey.MAX_CONNECTIONS.cast(25);
        String host = ConfigKey.SERVER_HOST.cast("example.org");
        Double limit = ConfigKey.RATE_LIMIT.cast(2.5);
        assertThat(connections).isEqualTo(25);
        assertThat(host).isEqualTo("example.org");
        assertThat(limit).isEqualTo(2.5);
        assertThat(ConfigKey.fromKey("enable.ssl")).contains(ConfigKey.ENABLE_SSL);
        assertThat(ConfigKey.fromKey("no.such.key")).isEmpty();
        assertThat(ConfigKey.fromKey("ENABLE_SSL")).isEmpty();
        assertThat(ConfigKey.fromKey("ENABLE.SSL")).isEmpty();
    }

    @Test
    void aWrongTypeFailsInsideCast() {
        assertThatThrownBy(() -> ConfigKey.MAX_CONNECTIONS.cast("ten")).isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> ConfigKey.ENABLE_SSL.cast("yes")).isInstanceOf(ClassCastException.class);
    }

    @Test
    void aDoubleKeyRefusesAnInteger() {
        assertThatThrownBy(() -> ConfigKey.RATE_LIMIT.cast(100)).isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> ConfigKey.MAX_CONNECTIONS.cast(25L)).isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> ConfigKey.MAX_CONNECTIONS.cast(2.5)).isInstanceOf(ClassCastException.class);
    }

    @Test
    void nullGivesTheDefault() {
        String host = ConfigKey.SERVER_HOST.cast(null);
        Integer connections = ConfigKey.MAX_CONNECTIONS.cast(null);
        assertThat(host).isEqualTo("localhost");
        assertThat(connections).isEqualTo(10);
    }

    @Test
    void findsAKeyBuiltAtRuntime() {
        String key = String.join(".", "rate", "limit");
        assertThat(ConfigKey.fromKey(key)).contains(ConfigKey.RATE_LIMIT);
    }
}
