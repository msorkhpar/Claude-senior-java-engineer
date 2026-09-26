package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CharsTest {

    @Test
    void readsTheEndAndTheStart() {
        assertThat(Chars.lastChar("Hello")).isEqualTo('o');
        assertThat(Chars.lastChar("a")).isEqualTo('a');
        assertThat(Chars.hasPrefix("abcdef", "abc")).isTrue();
        assertThat(Chars.hasPrefix("abxdef", "abc")).isFalse();
    }

    @Test
    void anEmptyStringHasNoLastChar() {
        assertThatThrownBy(() -> Chars.lastChar("")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aShortStringHasNoLongPrefix() {
        assertThat(Chars.hasPrefix("ab", "abc")).isFalse();
        assertThat(Chars.hasPrefix("", "a")).isFalse();
    }
}
