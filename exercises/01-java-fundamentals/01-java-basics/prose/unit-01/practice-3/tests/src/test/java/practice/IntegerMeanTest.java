package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntegerMeanTest {

    @Test
    void averagesTheValues() {
        assertThat(IntegerMean.mean(new int[]{2, 4, 6})).isEqualTo(4);
        assertThat(IntegerMean.mean(new int[]{1, 2})).isEqualTo(1);
        assertThat(IntegerMean.mean(new int[]{-3, -4})).isEqualTo(-3);
        assertThat(IntegerMean.mean(new int[]{9})).isEqualTo(9);
    }

    @Test
    void anEmptyArrayIsZero() {
        assertThat(IntegerMean.mean(new int[0])).isEqualTo(0);
    }

    @Test
    void theSumMayNotFitInAnInt() {
        assertThat(IntegerMean.mean(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE})).isEqualTo(Integer.MAX_VALUE);
        assertThat(IntegerMean.mean(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE})).isEqualTo(Integer.MIN_VALUE);
    }
}
