package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EditsTest {

    @Test
    void editsText() {
        assertThat(Edits.insertAt("Hello World", "Java ", 6)).isEqualTo("Hello Java World");
        assertThat(Edits.deleteRange("Hello World", 5, 11)).isEqualTo("Hello");
        assertThat(Edits.reversed("Hello")).isEqualTo("olleH");
    }

    @Test
    void deleteStopsBeforeEnd() {
        assertThat(Edits.deleteRange("abcdef", 1, 3)).isEqualTo("adef");
        assertThat(Edits.deleteRange("abc", 1, 1)).isEqualTo("abc");
    }

    @Test
    void insertCanAppendAtTheEnd() {
        assertThat(Edits.insertAt("Hello", "!", 5)).isEqualTo("Hello!");
        assertThat(Edits.insertAt("", "x", 0)).isEqualTo("x");
    }
}
