package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CodePointsTest {

    @Test
    void countsAndReversesPlainText() {
        assertThat(CodePoints.characters("abc")).isEqualTo(3);
        assertThat(CodePoints.characters("")).isZero();
        assertThat(CodePoints.reverse("abc")).isEqualTo("cba");
        assertThat(CodePoints.reverse("")).isEmpty();
    }

    @Test
    void anEmojiCountsOnce() {
        assertThat(CodePoints.characters("a\uD83D\uDE00")).isEqualTo(2);
        assertThat(CodePoints.characters("\uD83D\uDE00\uD83D\uDE00")).isEqualTo(2);
    }

    @Test
    void anEmojiStaysWholeWhenReversed() {
        assertThat(CodePoints.reverse("a\uD83D\uDE00")).isEqualTo("\uD83D\uDE00a");
        assertThat(CodePoints.reverse("\uD83D\uDE00b")).isEqualTo("b\uD83D\uDE00");
    }
}
