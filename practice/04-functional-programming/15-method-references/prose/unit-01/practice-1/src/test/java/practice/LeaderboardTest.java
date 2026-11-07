package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class LeaderboardTest {

    @Test
    void picksTheBestScoreOfARound() {
        assertThat(Leaderboard.bestScore(List.of(88, 23, 61))).contains(88);
        assertThat(Leaderboard.bestScore(List.of(57))).contains(57);
        assertThat(Leaderboard.bestScore(List.of())).isEmpty();
    }

    @Test
    void picksTheFewestMovesOfAPuzzle() {
        assertThat(Leaderboard.fewestMoves(List.of(14, 11, 3))).contains(3);
        assertThat(Leaderboard.fewestMoves(List.of(25))).contains(25);
        assertThat(Leaderboard.fewestMoves(List.of())).isEmpty();
    }

    @Test
    void theBestScoreCountsWhereverItWasPlayed() {
        assertThat(Leaderboard.bestScore(List.of(19, 42, 7, 33))).contains(42);
        assertThat(Leaderboard.bestScore(List.of(5, 12, 64))).contains(64);
    }

    @Test
    void theFewestMovesCountWhereverTheyWereMade() {
        assertThat(Leaderboard.fewestMoves(List.of(6, 21, 9))).contains(6);
        assertThat(Leaderboard.fewestMoves(List.of(17, 2, 30))).contains(2);
    }
}
