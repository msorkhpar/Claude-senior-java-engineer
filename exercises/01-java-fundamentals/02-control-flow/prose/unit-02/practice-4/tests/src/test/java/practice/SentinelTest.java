package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SentinelTest {

    @Test
    void addsTheReadings() {
        assertThat(Sentinel.total(new int[]{3, 4, 5})).isEqualTo(12);
        assertThat(Sentinel.total(new int[]{})).isZero();
    }

    @Test
    void aNegativeReadingIsSkipped() {
        assertThat(Sentinel.total(new int[]{3, -1, 4})).isEqualTo(7);
        assertThat(Sentinel.total(new int[]{-2, -2, 5})).isEqualTo(5);
    }

    @Test
    void zeroEndsTheData() {
        assertThat(Sentinel.total(new int[]{3, 4, 0, 10})).isEqualTo(7);
        assertThat(Sentinel.total(new int[]{0, 5})).isZero();
    }
}
