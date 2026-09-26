package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LongLineTest {

    private static final String Q = "\"\"\"";
    private static final String IN = "        ";

    /** Source lines joined at run time: every piece but the last ends with a continuation. */
    private static String source(String... pieces) {
        StringBuilder out = new StringBuilder(Q).append('\n');
        for (int i = 0; i < pieces.length; i++) {
            out.append(IN).append(pieces[i]).append(i == pieces.length - 1 ? Q : "\\\n");
        }
        return out.toString();
    }

    @Test
    void wrapsAfterSpaces() {
        assertThat(LongLine.source(String.join(" ", "This", "is", "one", "continuous", "line."), 15))
                .isEqualTo(source("This is one ", "continuous ", "line."));
        assertThat(LongLine.source(new String("short"), 20)).isEqualTo(source("short"));
        assertThat(LongLine.source(String.join(" ", "one", "two", "three"), 5))
                .isEqualTo(source("one ", "two ", "three"));
        assertThat(LongLine.source(String.join(" ", "https://api.example.org/v2/users", "?active=true"), 40))
                .isEqualTo(source("https://api.example.org/v2/users ", "?active=true"));
    }

    @Test
    void aLineMayFillTheWidthExactly() {
        assertThat(LongLine.source(String.join(" ", "This", "is", "one", "continuous", "line."), 12))
                .isEqualTo(source("This is one ", "continuous ", "line."));
        assertThat(LongLine.source(String.join(" ", "ab", "cd"), 5)).isEqualTo(source("ab cd"));
    }

    @Test
    void theBreakingSpaceCountsTowardsTheWidth() {
        assertThat(LongLine.source(String.join(" ", "This", "is", "one", "continuous", "line."), 11))
                .isEqualTo(source("This is ", "one ", "continuous ", "line."));
        assertThat(LongLine.source(String.join(" ", "ab", "cd", "ef"), 5)).isEqualTo(source("ab ", "cd ef"));
    }

    @Test
    void aLongWordIsNeverSplit() {
        assertThat(LongLine.source(String.join(" ", "a", "supercalifragilistic", "word"), 8))
                .isEqualTo(source("a ", "supercalifragilistic ", "word"));
        assertThat(LongLine.source(new String("unbreakable"), 4)).isEqualTo(source("unbreakable"));
    }
}
