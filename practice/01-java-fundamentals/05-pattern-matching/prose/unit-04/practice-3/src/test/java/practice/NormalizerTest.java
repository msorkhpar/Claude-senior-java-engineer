package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NormalizerTest {

    @Test
    void normalisesTextAndNumbers() {
        assertThat(Normalizer.text("  hi ")).isEqualTo("hi");
        assertThat(Normalizer.text(42)).isEqualTo("42");
        assertThat(Normalizer.text(3.5)).isEmpty();
    }

    @Test
    void nullIsEmptyToo() {
        assertThat(Normalizer.text(null)).isEmpty();
    }
}
