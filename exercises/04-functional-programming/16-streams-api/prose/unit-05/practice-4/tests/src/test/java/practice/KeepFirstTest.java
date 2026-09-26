package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class KeepFirstTest {

    @Test
    void dropsRepeatsInASequentialStream() {
        assertThat(KeepFirst.firstOccurrences(Stream.of(1, 2, 2, 3, 1))).containsExactly(1, 2, 3);
        assertThat(KeepFirst.firstOccurrences(Stream.of(4, 4, 4))).containsExactly(4);
        assertThat(KeepFirst.firstOccurrences(Stream.empty())).isEmpty();
    }

    @Test
    void keepsEncounterOrder() {
        assertThat(KeepFirst.firstOccurrences(Stream.of(30, 10, 30, 20, 10))).containsExactly(30, 10, 20);
    }

    @Test
    void parallelKeepsTheFirstCopies() {
        List<Integer> once = IntStream.range(0, 20_000).boxed().toList();
        List<Integer> twice = Stream.concat(IntStream.range(0, 20_000).boxed(), IntStream.range(0, 20_000).boxed()).toList();
        assertThat(KeepFirst.firstOccurrences(twice.parallelStream())).isEqualTo(once);
    }

    @Test
    void repeatsMatchByValue() {
        Integer first = Integer.valueOf(1000);
        Integer again = Integer.valueOf(1000);
        Integer other = Integer.valueOf(2000);
        assertThat(KeepFirst.firstOccurrences(Stream.of(first, other, again))).containsExactly(1000, 2000);
    }

    @Test
    void parallelKeepsEncounterOrderNotNumericOrder() {
        List<Integer> once = IntStream.range(0, 20_000).map(i -> 19_999 - i).boxed().toList();
        List<Integer> twice = Stream.concat(once.stream(), once.stream()).toList();
        assertThat(KeepFirst.firstOccurrences(twice.parallelStream())).isEqualTo(once);
    }
}
