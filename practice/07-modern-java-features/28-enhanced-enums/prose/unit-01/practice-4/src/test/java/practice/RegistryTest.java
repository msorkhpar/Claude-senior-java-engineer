package practice;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistryTest {

    private static Registry<Integer> medals() {
        Registry<Integer> registry = new Registry<>();
        registry.register("bronze", 3);
        registry.register("silver", 2);
        registry.register("gold", 1);
        return registry;
    }

    @Test
    void registersAndFindsEntries() {
        Registry<Integer> registry = medals();
        assertThat(registry.get("silver")).isEqualTo(2);
        assertThat(registry.get("gold")).isEqualTo(1);
        assertThat(registry.size()).isEqualTo(3);
        assertThat(registry.names()).containsExactlyInAnyOrder("bronze", "silver", "gold");
    }

    @Test
    void aTakenNameIsRefused() {
        Registry<Integer> registry = medals();
        assertThatThrownBy(() -> registry.register("gold", 9)).isInstanceOf(IllegalArgumentException.class);
        assertThat(registry.get("gold")).isEqualTo(1);
        assertThat(registry.size()).isEqualTo(3);
        String again = String.join("", "go", "ld");
        assertThatThrownBy(() -> registry.register(again, 9)).isInstanceOf(IllegalArgumentException.class);
        assertThat(registry.get("gold")).isEqualTo(1);
    }

    @Test
    void aMissingNameThrows() {
        Registry<Integer> registry = medals();
        assertThatThrownBy(() -> registry.get("platinum")).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void namesKeepRegistrationOrder() {
        assertThat(medals().names()).containsExactly("bronze", "silver", "gold");
    }

    @Test
    void findsANameBuiltAtRuntime() {
        Registry<Integer> registry = medals();
        String name = String.join("", "sil", "ver");
        assertThat(registry.get(name)).isEqualTo(2);
    }
}
