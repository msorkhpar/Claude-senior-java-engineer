package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TotalsTest {

    private static final List<Integer> ONE_TO_TEN_THOUSAND = IntStream.rangeClosed(1, 10_000).boxed().toList();
    private static final int SUM = 50_005_000;

    @Test
    void totalsASequentialStream() {
        assertThat(Totals.balance(Stream.of(10, 20, 30), 100)).isEqualTo(40);
        assertThat(Totals.balance(Stream.empty(), 50)).isEqualTo(50);
        assertThat(Totals.balance(ONE_TO_TEN_THOUSAND.stream(), 0)).isEqualTo(-SUM);
        assertThat(Totals.withBonus(Stream.of(1, 2, 3), 10)).isEqualTo(16);
        assertThat(Totals.withBonus(Stream.empty(), 5)).isEqualTo(5);
        assertThat(Totals.totalLength(Stream.of("a", "bb", "ccc"))).isEqualTo(6);
        assertThat(Totals.totalLength(Stream.empty())).isZero();
    }

    @Test
    void balanceHoldsInParallel() {
        assertThat(Totals.balance(ONE_TO_TEN_THOUSAND.parallelStream(), 0)).isEqualTo(-SUM);
        assertThat(Totals.balance(ONE_TO_TEN_THOUSAND.parallelStream(), 1_000)).isEqualTo(1_000 - SUM);
    }

    @Test
    void bonusCountedOnceInParallel() {
        assertThat(Totals.withBonus(ONE_TO_TEN_THOUSAND.parallelStream(), 7)).isEqualTo(SUM + 7);
    }

    @Test
    void lengthHoldsInParallel() {
        List<String> words = IntStream.range(0, 10_000).mapToObj(i -> "abc").toList();
        assertThat(Totals.totalLength(words.parallelStream())).isEqualTo(30_000);
    }
}
