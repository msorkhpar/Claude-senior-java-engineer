package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FloatPrecisionTest {

    @Test
    void smallValuesSurviveAndTheLessonsExampleDoesNot() {
        assertThat(FloatPrecision.fitsInFloat(100)).isTrue();
        assertThat(FloatPrecision.fitsInFloat(-42)).isTrue();
        assertThat(FloatPrecision.fitsInFloat(0)).isTrue();
        assertThat(FloatPrecision.fitsInFloat(123_456_789)).isFalse();
        assertThat(FloatPrecision.fitsInFloat(16_777_217)).isFalse();
    }

    @Test
    void someLargeValuesStillFit() {
        assertThat(FloatPrecision.fitsInFloat(16_777_216)).isTrue();
        assertThat(FloatPrecision.fitsInFloat(16_777_218)).isTrue();
        assertThat(FloatPrecision.fitsInFloat(1 << 30)).isTrue();
    }

    @Test
    void castingBackHidesTheLoss() {
        assertThat(FloatPrecision.fitsInFloat(Integer.MAX_VALUE)).isFalse();
        assertThat(FloatPrecision.fitsInFloat(Integer.MIN_VALUE)).isTrue();
    }
}
