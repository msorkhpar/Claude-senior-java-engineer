package practice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Picks the two standings a puzzle game shows after each round. */
public class Leaderboard {

    /** The highest score of the round, or empty when nobody played. */
    public static Optional<Integer> bestScore(List<Integer> scores) {
        return scores.stream().max(byScore());
    }

    /** The fewest moves anyone needed to solve the puzzle, or empty when nobody solved it. */
    public static Optional<Integer> fewestMoves(List<Integer> moves) {
        return moves.stream().min(byMoves());
    }

    // TODO: the order bestScore ranks scores in: of two scores, the higher one ranks higher.
    static Comparator<Integer> byScore() {
        throw new IllegalStateException("TODO: byScore");
    }

    // TODO: the order fewestMoves ranks move counts in: of two counts, the smaller one ranks lower.
    static Comparator<Integer> byMoves() {
        throw new IllegalStateException("TODO: byMoves");
    }
}
