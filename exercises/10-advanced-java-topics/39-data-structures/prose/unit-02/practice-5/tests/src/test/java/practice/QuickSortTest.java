package practice;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class QuickSortTest {

    private static final int N = 2048;
    private static final long BOUND = 4L * N * 11; // 4 n log2 n

    private static IntBinaryOperator counting(AtomicLong calls) {
        return (a, b) -> {
            calls.incrementAndGet();
            return Integer.compare(a, b);
        };
    }

    private static void assertSortedWithinBound(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);
        AtomicLong calls = new AtomicLong();

        assertThat(QuickSort.sort(input, counting(calls))).containsExactly(expected);
        assertThat(calls.get()).isLessThanOrEqualTo(BOUND);
    }

    @Test
    void sortsThePageArrays() {
        int[] random = {5, 2, 8, 1, 9, 3, 7, 4, 6};
        assertThat(QuickSort.sort(random, Integer::compare)).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertThat(random).containsExactly(5, 2, 8, 1, 9, 3, 7, 4, 6);

        assertThat(QuickSort.sort(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, Integer::compare))
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertThat(QuickSort.sort(new int[] {3, 1, 3, 2, 1, 3}, Integer::compare)).containsExactly(1, 1, 2, 3, 3, 3);
        assertThat(QuickSort.sort(new int[0], Integer::compare)).isEmpty();
        assertThat(QuickSort.sort(new int[] {4}, Integer::compare)).containsExactly(4);
    }

    @Test
    void sortedInputStaysNLogN() {
        assertSortedWithinBound(IntStream.range(0, N).toArray());
    }

    @Test
    void identicalValuesStayNLogN() {
        int[] same = new int[N];
        Arrays.fill(same, 7);
        assertSortedWithinBound(same);
    }

    @Test
    void reversedInputStaysNLogN() {
        assertSortedWithinBound(IntStream.range(0, N).map(i -> N - i).toArray());
    }
}
