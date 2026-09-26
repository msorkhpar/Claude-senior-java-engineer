package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequencesTest {

    @Test
    void makesEachSequence() {
        assertThat(Sequences.powersBelow(100)).containsExactly(1, 2, 4, 8, 16, 32, 64);
        assertThat(Sequences.repeated("ab", 3)).isEqualTo("ababab");
        assertThat(Sequences.repeated("ab", 0)).isEmpty();
        assertThat(Sequences.labels("row", 0)).isEmpty();
    }

    @Test
    void theBoundItselfIsExcluded() {
        assertThat(Sequences.powersBelow(64)).containsExactly(1, 2, 4, 8, 16, 32);
        assertThat(Sequences.powersBelow(2)).containsExactly(1);
        assertThat(Sequences.powersBelow(1)).isEmpty();
    }

    @Test
    void labelsIncludeTheLastNumber() {
        assertThat(Sequences.labels("row", 3)).containsExactly("row1", "row2", "row3");
        assertThat(Sequences.labels("n", 1)).containsExactly("n1");
    }
}
