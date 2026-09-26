package practice;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TextPartsTest {

    @Test
    void countsVowelsAndSplitsParts() {
        assertThat(TextParts.countVowels("hello world")).isEqualTo(3);
        assertThat(TextParts.countVowels("rhythm")).isZero();
        assertThat(TextParts.countVowels("")).isZero();
        assertThat(TextParts.parts("a, b ,c", ",")).containsExactly("a", "b", "c");
        assertThat(TextParts.parts("1 - 2", "-")).containsExactly("1", "2");
    }

    @Test
    void capitalVowelsCount() {
        assertThat(TextParts.countVowels("AEIOU")).isEqualTo(5);
        assertThat(TextParts.countVowels("Ice Age")).isEqualTo(4);
        Locale saved = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr"));
        try {
            assertThat(TextParts.countVowels("IGLOO")).isEqualTo(3);
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    void theDelimiterIsLiteral() {
        assertThat(TextParts.parts("1.2.3", ".")).containsExactly("1", "2", "3");
        assertThat(TextParts.parts("a|b", "|")).containsExactly("a", "b");
        assertThat(TextParts.parts("a<>b<c>d", "<>")).containsExactly("a", "b<c>d");
    }

    @Test
    void emptyPartsAreDropped() {
        assertThat(TextParts.parts("a,,b, ", ",")).containsExactly("a", "b");
        assertThat(TextParts.parts(new String(""), ",")).isEmpty();
    }
}
