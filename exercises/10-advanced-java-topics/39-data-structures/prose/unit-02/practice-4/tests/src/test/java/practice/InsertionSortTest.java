package practice;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsertionSortTest {

    private static IntBinaryOperator counting(AtomicLong calls) {
        return (a, b) -> {
            calls.incrementAndGet();
            return Integer.compare(a, b);
        };
    }

    @Test
    void sortsAndCountsTheWorstCase() {
        AtomicLong calls = new AtomicLong();
        assertThat(InsertionSort.sort(new int[] {5, 2, 8, 1, 9, 3, 7, 4, 6}, counting(calls)))
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);

        AtomicLong worst = new AtomicLong();
        assertThat(InsertionSort.sort(new int[] {5, 4, 3, 2, 1}, counting(worst))).containsExactly(1, 2, 3, 4, 5);
        assertThat(worst.get()).isEqualTo(10);

        assertThat(InsertionSort.sort(new int[0], counting(new AtomicLong()))).isEmpty();
    }

    @Test
    void sortedInputTakesOneComparisonPerElement() {
        int[] sorted = IntStream.rangeClosed(1, 1000).toArray();
        AtomicLong calls = new AtomicLong();

        assertThat(InsertionSort.sort(sorted, counting(calls))).containsExactly(sorted);
        assertThat(calls.get()).isEqualTo(999);
    }

    @Test
    void equalKeysKeepTheirOrder() {
        // compared by tens digit only: 21, 25, 23 are "equal", as are 13 and 11
        IntBinaryOperator byTens = (a, b) -> Integer.compare(a / 10, b / 10);

        assertThat(InsertionSort.sort(new int[] {21, 13, 25, 11, 23}, byTens)).containsExactly(13, 11, 21, 25, 23);
    }

    @Test
    void theInputIsLeftUnchanged() {
        int[] input = {3, 1, 2};

        int[] result = InsertionSort.sort(input, Integer::compare);

        assertThat(result).containsExactly(1, 2, 3);
        assertThat(input).containsExactly(3, 1, 2);
        assertThat(result).isNotSameAs(input);
    }
}
