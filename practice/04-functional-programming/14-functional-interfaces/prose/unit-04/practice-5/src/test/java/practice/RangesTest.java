package practice;

import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.assertj.core.api.Assertions.assertThat;

class RangesTest {

    @Test
    void selectsTheValuesThatPass() {
        IntPredicate even = n -> n % 2 == 0;
        assertThat(Ranges.select(1, 6, even)).containsExactly(2, 4, 6);
        assertThat(Ranges.select(-5, 5, Ranges.between(1, 5).and(even))).containsExactly(2, 4);
        assertThat(Ranges.select(-10, 10, Ranges.outside(-5, 5).and(n -> n > 7))).containsExactly(8, 9, 10);
        assertThat(Ranges.select(3, 3, n -> true)).containsExactly(3);
    }

    @Test
    void bothBoundsAreInside() {
        IntPredicate range = Ranges.between(1, 5);
        assertThat(range.test(1)).isTrue();
        assertThat(range.test(5)).isTrue();
        assertThat(range.test(0)).isFalse();
        assertThat(range.test(6)).isFalse();
    }

    @Test
    void outsideIsTheExactComplement() {
        assertThat(Ranges.select(-2, 2, Ranges.outside(-1, 1))).containsExactly(-2, 2);
        assertThat(Ranges.outside(0, 5).test(0)).isFalse();
        assertThat(Ranges.outside(0, 5).test(5)).isFalse();
        assertThat(Ranges.outside(0, 5).test(-1)).isTrue();
    }

    @Test
    void aBackwardsRangeSelectsNothing() {
        assertThat(Ranges.select(5, 1, n -> true)).isEmpty();
    }
}
