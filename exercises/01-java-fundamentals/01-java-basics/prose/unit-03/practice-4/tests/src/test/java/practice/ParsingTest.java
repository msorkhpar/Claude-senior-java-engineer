package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParsingTest {

    @Test
    void parsesPlainNumbers() {
        assertThat(Parsing.parseOrNull("123")).isEqualTo(123);
        assertThat(Parsing.parseOrNull("-7")).isEqualTo(-7);
        assertThat(Parsing.parseOrDefault("42", 0)).isEqualTo(42);
    }

    @Test
    void textThatIsNoNumberIsNull() {
        assertThat(Parsing.parseOrNull("abc")).isNull();
        assertThat(Parsing.parseOrNull("")).isNull();
        assertThat(Parsing.parseOrNull("12.5")).isNull();
        assertThat(Parsing.parseOrNull(null)).isNull();
    }

    @Test
    void aNumberBeyondIntIsNull() {
        assertThat(Parsing.parseOrNull("2147483648")).isNull();
        assertThat(Parsing.parseOrNull("-2147483649")).isNull();
        assertThat(Parsing.parseOrNull("2147483647")).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void surroundingSpacesAreIgnored() {
        assertThat(Parsing.parseOrNull(" 42 ")).isEqualTo(42);
        assertThat(Parsing.parseOrNull("\t-3\n")).isEqualTo(-3);
    }

    @Test
    void theDefaultStandsInForNoNumber() {
        assertThat(Parsing.parseOrDefault("oops", 5)).isEqualTo(5);
        assertThat(Parsing.parseOrDefault(null, -1)).isEqualTo(-1);
    }
}
