package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AbbreviationTest {

    @Test
    void shortensALongText() {
        assertThat(Abbreviation.abbreviate("Hello, World", 8)).isEqualTo("Hello...");
        assertThat(Abbreviation.abbreviate("abcdefghij", 5)).isEqualTo("ab...");
    }

    @Test
    void aShortTextIsKept() {
        assertThat(Abbreviation.abbreviate("Hi", 8)).isEqualTo("Hi");
        assertThat(Abbreviation.abbreviate("", 5)).isEmpty();
    }

    @Test
    void aTextOfExactlyMaxIsKept() {
        assertThat(Abbreviation.abbreviate("Exactly8", 8)).isEqualTo("Exactly8");
    }

    @Test
    void tooSmallAMaxIsRefused() {
        assertThatThrownBy(() -> Abbreviation.abbreviate("Hello", 2)).isInstanceOf(IllegalArgumentException.class);
    }
}
