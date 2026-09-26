package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineCountTest {

    @Test
    void countsTheLines() {
        assertThat(LineCount.count(String.join("\n", "Line 1", "Line 2", "Line 3"))).isEqualTo(3);
        assertThat(LineCount.count(new String("Hello"))).isEqualTo(1);
        assertThat(LineCount.count(String.join("\n", "a", "", "b"))).isEqualTo(3);
    }

    @Test
    void aTrailingNewlineEndsTheLastLine() {
        assertThat(LineCount.count(String.join("\n", "Hello", ""))).isEqualTo(1);
        assertThat(LineCount.count(String.join("\n", "Hello", "World", ""))).isEqualTo(2);
    }

    @Test
    void blankLinesAtTheEndStillCount() {
        assertThat(LineCount.count(String.join("\n", "a", "", ""))).isEqualTo(2);
        assertThat(LineCount.count(String.join("\n", "", ""))).isEqualTo(1);
    }

    @Test
    void everyLineTerminatorEndsALine() {
        assertThat(LineCount.count(String.join("\r\n", "a", "b"))).isEqualTo(2);
        assertThat(LineCount.count(String.join("\r", "a", "b", "c"))).isEqualTo(3);
        assertThat(LineCount.count(String.join("\r\n", "Hello", ""))).isEqualTo(1);
    }

    @Test
    void anEmptyBlockHasNoLines() {
        assertThat(LineCount.count(new String(""))).isZero();
    }
}
