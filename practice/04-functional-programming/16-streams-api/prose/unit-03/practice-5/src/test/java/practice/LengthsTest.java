package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LengthsTest {

    @Test
    void computesTotalsAndAverages() {
        assertThat(Lengths.totalLength(List.of("hello", "world", "!"))).isEqualTo(11);
        assertThat(Lengths.totalLength(List.of())).isZero();
        assertThat(Lengths.average(List.of("2", "4", "6"))).isEqualTo(OptionalDouble.of(4.0));
        assertThat(Lengths.average(List.of("1", "2"))).isEqualTo(OptionalDouble.of(1.5));
        assertThat(Lengths.sum(List.of(1, 2, 3))).isEqualTo(6L);
        assertThatThrownBy(() -> Lengths.average(List.of("2", "2.5"))).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void anEmptyListHasNoAverage() {
        assertThat(Lengths.average(List.of())).isEmpty();
    }

    @Test
    void largeSumsDoNotOverflow() {
        assertThat(Lengths.sum(List.of(Integer.MAX_VALUE, Integer.MAX_VALUE))).isEqualTo(2L * Integer.MAX_VALUE);
        String max = String.valueOf(Integer.MAX_VALUE);
        assertThat(Lengths.average(List.of(max, max))).isEqualTo(OptionalDouble.of(Integer.MAX_VALUE));
    }
}
