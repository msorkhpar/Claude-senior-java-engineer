package practice;

import org.junit.jupiter.api.Test;

import practice.TypedConfig.Key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypedConfigTest {

    @Test
    void storesEachValueUnderItsTypedKey() {
        TypedConfig config = new TypedConfig();
        config.put(TypedConfig.PORT, 9090);
        config.put(TypedConfig.HOST, "example.org");
        Integer port = config.get(TypedConfig.PORT);
        String host = config.get(TypedConfig.HOST);
        assertThat(port).isEqualTo(9090);
        assertThat(host).isEqualTo("example.org");
        assertThat(config.size()).isEqualTo(2);
        assertThat(config.contains(TypedConfig.PORT)).isTrue();
    }

    @Test
    void anAbsentKeyGivesItsDefault() {
        TypedConfig config = new TypedConfig();
        config.put(TypedConfig.PORT, 9090);
        String host = config.get(TypedConfig.HOST);
        assertThat(host).isEqualTo("localhost");
        assertThat(config.contains(TypedConfig.HOST)).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void aWrongTypeIsRefusedOnPut() {
        TypedConfig config = new TypedConfig();
        Key raw = TypedConfig.PORT;
        assertThatThrownBy(() -> config.put(raw, "eighty")).isInstanceOf(ClassCastException.class);
        assertThat(config.size()).isZero();
    }

    @Test
    void aNullValueIsRefused() {
        TypedConfig config = new TypedConfig();
        assertThatThrownBy(() -> config.put(TypedConfig.HOST, null)).isInstanceOf(NullPointerException.class);
        assertThat(config.size()).isZero();
    }

    @Test
    void anEqualKeyFindsTheValue() {
        TypedConfig config = new TypedConfig();
        Key<Integer> port = new Key<>(String.join("", "po", "rt"), Integer.class, Integer.parseInt("8080"));
        config.put(port, 9090);
        Integer found = config.get(TypedConfig.PORT);
        assertThat(found).isEqualTo(9090);
        assertThat(config.contains(TypedConfig.PORT)).isTrue();
        Key<String> otherPort = new Key<>("port", String.class, "none");
        assertThat(config.contains(otherPort)).as("same name, other type: a different key").isFalse();
        String other = config.get(otherPort);
        assertThat(other).isEqualTo("none");
    }
}
