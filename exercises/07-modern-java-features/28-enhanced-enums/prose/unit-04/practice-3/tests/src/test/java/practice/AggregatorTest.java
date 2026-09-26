package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregatorTest {

    @Test
    void aggregatesAnyKindOfNumber() {
        assertThat(Aggregator.SUM.aggregate(List.of(1, 2, 3))).isEqualTo(6.0);
        assertThat(Aggregator.AVERAGE.aggregate(List.of(1.5, 2.5))).isEqualTo(2.0);
        assertThat(Aggregator.MIN.aggregate(List.of(7, 3, 5))).isEqualTo(3.0);
        assertThat(Aggregator.MAX.aggregate(List.of(100L, 200L))).isEqualTo(200.0);
        assertThat(Aggregator.COUNT.aggregate(List.of(4, 4, 4))).isEqualTo(3.0);
        assertThat(Aggregator.MIN.aggregate(List.of(1.5, 2.5))).isEqualTo(1.5);
        assertThat(Aggregator.MAX.aggregate(List.of(0.5))).isEqualTo(0.5);
        assertThat(Aggregator.MAX.aggregate(List.of(-3, -1))).isEqualTo(-1.0);
        assertThat(Aggregator.AVERAGE.aggregate(List.of(1, 2))).isEqualTo(1.5);
    }

    @Test
    void mixedNumbersKeepTheirFractions() {
        assertThat(Aggregator.SUM.aggregate(List.<Number>of(1, 2.5, 3L))).isEqualTo(6.5);
    }

    @Test
    void anEmptyListSumsAndAveragesToZero() {
        assertThat(Aggregator.SUM.aggregate(List.<Integer>of())).isEqualTo(0.0);
        assertThat(Aggregator.AVERAGE.aggregate(List.<Integer>of())).isEqualTo(0.0);
    }

    @Test
    void anEmptyListHasNoMinOrMax() {
        assertThatThrownBy(() -> Aggregator.MIN.aggregate(List.<Double>of())).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> Aggregator.MAX.aggregate(List.<Double>of())).isInstanceOf(NoSuchElementException.class);
    }
}
