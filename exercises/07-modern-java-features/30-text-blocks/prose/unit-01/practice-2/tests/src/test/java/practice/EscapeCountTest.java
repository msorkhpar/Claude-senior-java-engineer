package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscapeCountTest {

    @Test
    void countsEachEscape() {
        assertThat(EscapeCount.count("{\\\"name\\\": \\\"Alice\\\"}")).isEqualTo(4);
        assertThat(EscapeCount.count("Line 1\\n\\tIndented")).isEqualTo(2);
        assertThat(EscapeCount.count("He said \\\"Hi\\\"")).isEqualTo(2);
        assertThat(EscapeCount.count("plain text")).isEqualTo(0);
        assertThat(EscapeCount.count("")).isEqualTo(0);
        assertThat(EscapeCount.count("It\\'s\\r\\b")).isEqualTo(3);
    }

    @Test
    void aDoubledBackslashIsOneEscape() {
        assertThat(EscapeCount.count("C:\\\\Users\\\\admin")).isEqualTo(2);
        assertThat(EscapeCount.count("\\\\d+")).isEqualTo(1);
    }

    @Test
    void anEscapedBackslashEndsBeforeTheNextLetter() {
        assertThat(EscapeCount.count("\\\\n")).isEqualTo(1);
        assertThat(EscapeCount.count("C:\\\\new\\\\table")).isEqualTo(2);
        assertThat(EscapeCount.count("\\\\\\t")).isEqualTo(2);
    }
}
