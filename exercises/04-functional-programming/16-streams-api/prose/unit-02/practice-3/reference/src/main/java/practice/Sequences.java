package practice;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Sequences {

    private Sequences() {
    }

    /** Returns the powers of two 1, 2, 4, ... that are less than {@code bound}. */
    public static List<Integer> powersBelow(int bound) {
        return Stream.iterate(1, n -> n < bound, n -> n * 2).toList();
    }

    /** Returns {@code text} repeated {@code times} times. */
    public static String repeated(String text, int times) {
        return Stream.generate(() -> text).limit(times).collect(Collectors.joining());
    }

    /** Returns prefix + 1 up to prefix + count. */
    public static List<String> labels(String prefix, int count) {
        return IntStream.rangeClosed(1, count).mapToObj(i -> prefix + i).toList();
    }
}
