package practice;

import java.util.List;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoreboardTest {

    private static Scoreboard page() {
        return new Scoreboard(List.of(90, 50, 100, 70, 95, 85));
    }

    @Test
    void answersThePageQueries() {
        Scoreboard board = page();

        assertThat(board.between(60, 92)).containsExactly(70, 85, 90);
        assertThat(board.atLeast(80)).isEqualTo(OptionalInt.of(85));
        assertThat(board.atMost(80)).isEqualTo(OptionalInt.of(70));
        assertThatThrownBy(() -> board.between(95, 70)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bothEndsOfARangeAreIncluded() {
        assertThat(page().between(70, 95)).containsExactly(70, 85, 90, 95);
    }

    @Test
    void anExactMatchIsItsOwnNeighbour() {
        Scoreboard board = page();

        assertThat(board.atLeast(85)).isEqualTo(OptionalInt.of(85));
        assertThat(board.atMost(85)).isEqualTo(OptionalInt.of(85));
    }

    @Test
    void noNeighbourGivesAnEmptyResult() {
        Scoreboard board = page();

        assertThat(board.atLeast(101)).isEmpty();
        assertThat(board.atMost(10)).isEmpty();
        assertThat(board.between(71, 84)).isEmpty();
    }

    @Test
    void aScoreIsKeptOnce() {
        Scoreboard board = new Scoreboard(List.of(90, 70, 90, 70, 85));

        assertThat(board.between(0, 1000)).containsExactly(70, 85, 90);
    }
}
