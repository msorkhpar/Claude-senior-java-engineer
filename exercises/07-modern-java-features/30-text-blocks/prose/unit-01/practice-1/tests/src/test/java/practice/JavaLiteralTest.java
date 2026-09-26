package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaLiteralTest {

    /** A copy made at run time, so no answer can lean on the string pool. */
    private static String fresh(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void quotesAreEscaped() {
        assertThat(JavaLiteral.quote(fresh("He said \"Hi\""))).isEqualTo(fresh("\"He said \\\"Hi\\\"\""));
        assertThat(JavaLiteral.quote(fresh("{\"name\": \"Alice\"}")))
                .isEqualTo(fresh("\"{\\\"name\\\": \\\"Alice\\\"}\""));
        assertThat(JavaLiteral.quote(fresh("plain"))).isEqualTo(fresh("\"plain\""));
        assertThat(JavaLiteral.quote(fresh(""))).isEqualTo(fresh("\"\""));
    }

    @Test
    void backslashesAreDoubled() {
        assertThat(JavaLiteral.quote(fresh("C:\\Users\\admin"))).isEqualTo(fresh("\"C:\\\\Users\\\\admin\""));
        assertThat(JavaLiteral.quote(fresh("\\d+"))).isEqualTo(fresh("\"\\\\d+\""));
    }

    @Test
    void lineBreaksBecomeEscapes() {
        assertThat(JavaLiteral.quote(fresh("Line 1\n\tIndented"))).isEqualTo(fresh("\"Line 1\\n\\tIndented\""));
        assertThat(JavaLiteral.quote(fresh("a\nb\n"))).isEqualTo(fresh("\"a\\nb\\n\""));
        assertThat(JavaLiteral.quote(fresh("a\r\nb"))).isEqualTo(fresh("\"a\\r\\nb\""));
    }

    @Test
    void singleQuotesStayAsIs() {
        assertThat(JavaLiteral.quote(fresh("It's"))).isEqualTo(fresh("\"It's\""));
        assertThat(JavaLiteral.quote(fresh("'O''Brien'"))).isEqualTo(fresh("\"'O''Brien'\""));
    }
}
