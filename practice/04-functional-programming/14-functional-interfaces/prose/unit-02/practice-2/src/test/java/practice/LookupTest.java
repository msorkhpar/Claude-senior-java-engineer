package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class LookupTest {

    @Test
    void takesTheFirstSourceThatHasAValue() {
        assertThat(Lookup.resolve(Optional.of("cache"), () -> Optional.of("db"), () -> "default")).isEqualTo("cache");
        assertThat(Lookup.resolve(Optional.empty(), () -> Optional.of("db"), () -> "default")).isEqualTo("db");
        assertThat(Lookup.resolve(Optional.<String>empty(), Optional::empty, () -> "default")).isEqualTo("default");
    }

    @Test
    void theSecondaryIsNotAskedWhenThePrimaryHasAValue() {
        List<String> asked = new ArrayList<>();
        Supplier<Optional<String>> secondary = () -> {
            asked.add("secondary");
            return Optional.of("db");
        };

        assertThat(Lookup.resolve(Optional.of("cache"), secondary, () -> "default")).isEqualTo("cache");
        assertThat(asked).isEmpty();
    }

    @Test
    void theFallbackIsNotBuiltWhenAValueExists() {
        List<String> asked = new ArrayList<>();
        Supplier<String> fallback = () -> {
            asked.add("fallback");
            return "default";
        };

        assertThat(Lookup.resolve(Optional.of("cache"), Optional::empty, fallback)).isEqualTo("cache");
        assertThat(Lookup.resolve(Optional.empty(), () -> Optional.of("db"), fallback)).isEqualTo("db");
        assertThat(asked).isEmpty();
    }

    @Test
    void theSecondaryIsAskedAtMostOnce() {
        List<String> asked = new ArrayList<>();
        Supplier<Optional<String>> secondary = () -> {
            asked.add("secondary");
            return Optional.of("db");
        };

        assertThat(Lookup.resolve(Optional.empty(), secondary, () -> "default")).isEqualTo("db");
        assertThat(asked).containsExactly("secondary");
    }

    @Test
    void aNullFallbackIsReturnedAsIs() {
        assertThat(Lookup.resolve(Optional.<String>empty(), Optional::empty, () -> null)).isNull();
    }
}
