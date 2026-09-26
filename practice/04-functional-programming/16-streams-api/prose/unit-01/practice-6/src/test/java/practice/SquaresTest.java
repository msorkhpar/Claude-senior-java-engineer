package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquaresTest {

    @Test
    void listsTheFirstSquares() {
        assertThat(Squares.firstSquaresAbove(0, 4)).containsExactly(1, 4, 9, 16);
        assertThat(Squares.firstSquaresAbove(0, 1)).containsExactly(1);
        assertThat(Squares.firstSquaresAbove(0, 0)).isEmpty();
    }

    @Test
    void skipsSquaresNotAboveTheThreshold() {
        assertThat(Squares.firstSquaresAbove(10, 3)).containsExactly(16, 25, 36);
        assertThat(Squares.firstSquaresAbove(1000, 2)).containsExactly(1024, 1089);
    }

    @Test
    void theThresholdItselfIsNotAbove() {
        assertThat(Squares.firstSquaresAbove(16, 2)).containsExactly(25, 36);
    }
}
