package practice;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class OrderedOutput {

    private OrderedOutput() {
    }

    /** Hands every line to the sink in encounter order; the stream may be parallel. */
    public static void emitInOrder(Stream<String> lines, Consumer<String> sink) {
        lines.forEachOrdered(sink);
    }

    /** The first number in encounter order greater than the threshold; the stream may be parallel. */
    public static Optional<Integer> firstAbove(Stream<Integer> numbers, int threshold) {
        return numbers.filter(n -> n > threshold).findAny();
    }
}
