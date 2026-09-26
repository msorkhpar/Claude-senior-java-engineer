package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OnePassTest {

    private static final List<Integer> DIGITS = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3);

    @Test
    void findsRangeAndMean() {
        assertThat(OnePass.range(DIGITS)).isEqualTo(Optional.of(new OnePass.MinMax(1, 9)));
        assertThat(OnePass.range(List.of(7))).isEqualTo(Optional.of(new OnePass.MinMax(7, 7)));
        assertThat(OnePass.mean(DIGITS)).isCloseTo(3.9, within(1e-9));
        assertThat(OnePass.mean(List.of(1, 2))).isCloseTo(1.5, within(1e-9));
    }

    @Test
    void rangeOfNothingIsEmpty() {
        assertThat(OnePass.range(List.of())).isEmpty();
    }

    @Test
    void meanOfNothingIsZero() {
        assertThat(OnePass.mean(List.of())).isEqualTo(0.0);
    }

    @Test
    void rangeOfNegatives() {
        assertThat(OnePass.range(List.of(-4, -2, -8))).isEqualTo(Optional.of(new OnePass.MinMax(-8, -2)));
    }
}
