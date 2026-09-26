package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderedOutputTest {

    @Test
    void handlesASequentialStream() {
        List<String> seen = new ArrayList<>();
        OrderedOutput.emitInOrder(Stream.of("a", "b", "c"), seen::add);
        assertThat(seen).containsExactly("a", "b", "c");
        assertThat(OrderedOutput.firstAbove(Stream.of(5, 3, 8, 1, 9, 2, 7, 4, 6), 5)).isEqualTo(Optional.of(8));
        assertThat(OrderedOutput.firstAbove(Stream.of(1, 2), 5)).isEmpty();
    }

    @Test
    void linesKeepOrderInParallel() {
        List<String> lines = IntStream.range(0, 50_000).mapToObj(i -> "line " + i).toList();
        List<String> seen = Collections.synchronizedList(new ArrayList<>());
        OrderedOutput.emitInOrder(lines.parallelStream(), seen::add);
        assertThat(seen).isEqualTo(lines);
    }

    @Test
    void firstMatchInParallelIsTheFirst() {
        List<Integer> numbers = IntStream.rangeClosed(1, 1_000_000).boxed().toList();
        assertThat(OrderedOutput.firstAbove(numbers.parallelStream(), 0)).isEqualTo(Optional.of(1));
        assertThat(OrderedOutput.firstAbove(numbers.parallelStream(), 500_000)).isEqualTo(Optional.of(500_001));
    }
}
