package practice;

import java.util.List;
import java.util.stream.Stream;

public final class Squares {

    private Squares() {
    }

    /** Returns the first {@code count} squares of 1, 2, 3, ... that are greater than {@code threshold}. */
    public static List<Integer> firstSquaresAbove(int threshold, int count) {
        return Stream.iterate(1, n -> n + 1)
                .map(n -> n * n)
                .filter(square -> square > threshold)
                .limit(count)
                .toList();
    }
}
