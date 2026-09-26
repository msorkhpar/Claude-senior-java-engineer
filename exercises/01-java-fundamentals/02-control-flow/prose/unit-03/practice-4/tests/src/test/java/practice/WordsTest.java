package practice;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.*;

class WordsTest {

    private static int words(String text) {
        try {
            return Words.count(new BufferedReader(new StringReader(text)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void countsTheWordsOfEveryLine() {
        assertThat(words("one two\nthree")).isEqualTo(3);
        assertThat(words("to be or\nnot to\nbe")).isEqualTo(6);
    }

    @Test
    void blankLinesHoldNoWords() {
        assertThat(words("a\n\n   \nb")).isEqualTo(2);
    }

    @Test
    void extraSpacesSeparateNothing() {
        assertThat(words("  a   b  ")).isEqualTo(2);
        assertThat(words("a\tb")).isEqualTo(2);
    }

    @Test
    void anEmptyInputHoldsNoWords() {
        assertThat(words("")).isZero();
    }
}
