package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EscapesTest {

    /** A string made at run time from its characters. */
    private static String chars(char... cs) {
        return new String(cs);
    }

    @Test
    void decodesTheCommonEscapes() {
        String raw = "Hello\\nWorld\\tTab";
        assertThat(raw).hasSize(17);
        assertThat(Escapes.decode(raw)).isEqualTo(String.join("", "Hello", "\n", "World", "\t", "Tab")).hasSize(15);
        assertThat(Escapes.decode("say \\\"hi\\\" and \\'bye\\'")).isEqualTo(chars('s', 'a', 'y', ' ', '"', 'h', 'i', '"',
                ' ', 'a', 'n', 'd', ' ', '\'', 'b', 'y', 'e', '\''));
        assertThat(Escapes.decode("Octal A: \\101")).isEqualTo(String.join("", "Octal A: ", "A"));
        assertThat(Escapes.decode("a\\rb\\bc\\fd")).isEqualTo(chars('a', '\r', 'b', '\b', 'c', '\f', 'd'));
        assertThat(Escapes.decode("no escapes")).isEqualTo(String.join(" ", "no", "escapes"));
    }

    @Test
    void anEscapedBackslashStaysOneBackslash() {
        assertThat(Escapes.decode("path=C:\\\\Users\\\\admin")).isEqualTo(String.join("\\", "path=C:", "Users", "admin"));
        assertThat(Escapes.decode("C:\\\\new")).isEqualTo(String.join("\\", "C:", "new"));
        assertThat(Escapes.decode("\\\\t")).isEqualTo(chars('\\', 't'));
    }

    @Test
    void theSpaceEscapeBecomesASpace() {
        assertThat(Escapes.decode("a\\sb")).isEqualTo(String.join(" ", "a", "b"));
        assertThat(Escapes.decode("end\\s")).isEqualTo(String.join("", "end", " "));
    }

    @Test
    void aBackslashBeforeALineBreakJoinsTheLines() {
        assertThat(Escapes.decode(String.join("\n", "Hello \\", "World"))).isEqualTo(String.join(" ", "Hello", "World"));
        assertThat(Escapes.decode(String.join("\r\n", "a\\", "b"))).isEqualTo(String.join("", "a", "b"));
    }

    @Test
    void anInvalidEscapeIsRefused() {
        assertThatThrownBy(() -> Escapes.decode("Invalid \\x escape")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Escapes.decode("\\q")).isInstanceOf(IllegalArgumentException.class);
    }
}
