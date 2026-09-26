package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DescriberTest {

    @Test
    void describesTextAndOtherValues() {
        assertThat(Describer.describe("hello")).isEqualTo("text of 5");
        assertThat(Describer.describe("")).isEqualTo("text of 0");
        assertThat(Describer.describe(42)).isEqualTo("not text");
    }

    @Test
    void nullHasItsOwnAnswer() {
        assertThat(Describer.describe(null)).isEqualTo("nothing");
    }
}
