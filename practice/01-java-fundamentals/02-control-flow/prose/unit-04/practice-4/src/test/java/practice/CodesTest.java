package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CodesTest {

    @Test
    void namesEachCode() {
        assertThat(Codes.name("AP")).isEqualTo("apple");
        assertThat(Codes.name("BN")).isEqualTo("banana");
        assertThat(Codes.name("CH")).isEqualTo("cherry");
        assertThat(Codes.name("XX")).isEqualTo("unknown");
    }

    @Test
    void codesAreCaseSensitive() {
        assertThat(Codes.name("ap")).isEqualTo("unknown");
        assertThat(Codes.name("Bn")).isEqualTo("unknown");
    }

    @Test
    void nullIsMissing() {
        assertThat(Codes.name(null)).isEqualTo("missing");
    }
}
