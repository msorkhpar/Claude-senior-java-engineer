package practice;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BinarySearchTest {

    /** Counts reads; refuses an index outside the sequence. */
    private static final class Counted implements BinarySearch.SortedInts {
        private final int size;
        private final java.util.function.IntUnaryOperator valueAt;
        int reads;

        Counted(int size, java.util.function.IntUnaryOperator valueAt) {
            this.size = size;
            this.valueAt = valueAt;
        }

        static Counted of(int... values) {
            int[] copy = values.clone();
            return new Counted(copy.length, i -> copy[i]);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int get(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("index " + index + " of " + size);
            }
            reads++;
            return valueAt.applyAsInt(index);
        }
    }

    @Test
    void findsValuesInThePageArray() {
        assertThat(BinarySearch.indexOf(Counted.of(1, 3, 5, 7, 9), 7)).isEqualTo(3);
        assertThat(BinarySearch.indexOf(Counted.of(1, 3, 5, 7, 9), 1)).isEqualTo(0);
        assertThat(BinarySearch.indexOf(Counted.of(1, 3, 5, 7, 9), 4)).isEqualTo(-1);
        assertThat(BinarySearch.indexOf(Counted.of(), 7)).isEqualTo(-1);
    }

    @Test
    void aMillionValuesTakeAtMostTwentyProbes() {
        int n = 1_000_000;
        for (int target : new int[] {0, 2, 999_998, 1_999_998, 1_234_568, 777_777, -5, 2_000_001}) {
            Counted values = new Counted(n, i -> 2 * i);
            int expected = target >= 0 && target % 2 == 0 && target / 2 < n ? target / 2 : -1;

            assertThat(BinarySearch.indexOf(values, target)).as("target %d", target).isEqualTo(expected);
            assertThat(values.reads).as("reads for target %d", target).isLessThanOrEqualTo(20);
        }
    }

    @Test
    void theMiddleOfAHugeRangeDoesNotOverflow() {
        int n = Integer.MAX_VALUE - 1;
        Counted values = new Counted(n, i -> i);

        assertThat(BinarySearch.indexOf(values, n - 3)).isEqualTo(n - 3);
        assertThat(values.reads).isLessThanOrEqualTo(32);
    }

    @Test
    void aOneElementRangeIsStillChecked() {
        assertThat(BinarySearch.indexOf(Counted.of(7), 7)).isEqualTo(0);
        assertThat(BinarySearch.indexOf(Counted.of(7), 3)).isEqualTo(-1);
        assertThat(BinarySearch.indexOf(Counted.of(1, 3, 5, 7, 9), 9)).isEqualTo(4);
    }
}
