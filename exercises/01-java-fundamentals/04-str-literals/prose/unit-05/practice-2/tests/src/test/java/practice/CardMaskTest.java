package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CardMaskTest {

    @Test
    void masksAllButTheLastFour() {
        assertThat(CardMask.mask("1234567812345678")).isEqualTo("************5678");
        assertThat(CardMask.mask("98765")).isEqualTo("*8765");
    }

    @Test
    void separatorsStayInPlace() {
        assertThat(CardMask.mask("1234 5678 9012 3456")).isEqualTo("**** **** **** 3456");
        assertThat(CardMask.mask("12-34-56")).isEqualTo("**-34-56");
    }

    @Test
    void aShortNumberIsNotMasked() {
        assertThat(CardMask.mask("123")).isEqualTo("123");
        assertThat(CardMask.mask("")).isEmpty();
    }
}
