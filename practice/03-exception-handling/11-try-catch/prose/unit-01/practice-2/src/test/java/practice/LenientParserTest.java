package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LenientParserTest {

    @Test
    void parsesNumbers() {
        assertThat(LenientParser.parseOrDefault("42", 0)).isEqualTo(42);
        assertThat(LenientParser.parseOrDefault(" -7 ", 0)).isEqualTo(-7);
        assertThat(LenientParser.parseOrDefault("+5", 0)).isEqualTo(5);
        assertThat(LenientParser.parseOrDefault("2147483647", 0)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void returnsTheFallbackForText() {
        assertThat(LenientParser.parseOrDefault("abc", -1)).isEqualTo(-1);
        assertThat(LenientParser.parseOrDefault("", 9)).isEqualTo(9);
        assertThat(LenientParser.parseOrDefault("4.5", 3)).isEqualTo(3);
    }

    @Test
    void aNullTextIsAnErrorNotAFallback() {
        assertThatThrownBy(() -> LenientParser.parseOrDefault(null, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void anOutOfRangeNumberGetsTheFallback() {
        assertThat(LenientParser.parseOrDefault("2147483648", -1)).isEqualTo(-1);
        assertThat(LenientParser.parseOrDefault("99999999999", 0)).isEqualTo(0);
    }
}
