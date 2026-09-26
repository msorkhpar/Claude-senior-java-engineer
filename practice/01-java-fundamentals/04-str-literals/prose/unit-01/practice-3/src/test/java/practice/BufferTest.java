package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BufferTest {

    @Test
    void takesAWordFromTheStart() {
        char[] buffer = "Hello you".toCharArray();
        assertThat(Buffer.word(buffer, 0, 5)).isEqualTo("Hello");
        assertThat(Buffer.word(buffer, 0, 1)).isEqualTo("H");
    }

    @Test
    void takesAWordFromTheMiddle() {
        char[] buffer = "Hello you".toCharArray();
        assertThat(Buffer.word(buffer, 6, 9)).isEqualTo("you");
        assertThat(Buffer.word(buffer, 1, 4)).isEqualTo("ell");
    }

    @Test
    void anEmptyRangeIsEmpty() {
        char[] buffer = "Hello you".toCharArray();
        assertThat(Buffer.word(buffer, 3, 3)).isEmpty();
    }
}
