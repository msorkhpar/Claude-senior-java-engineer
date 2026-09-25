package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearlyEqualTest {

    @Test
    void comparesWithATolerance() {
        assertThat(NearlyEqual.nearlyEqual(0.1 + 0.2, 0.3)).isTrue();
        assertThat(NearlyEqual.nearlyEqual(0.7 + 0.1, 0.8)).isTrue();
        assertThat(NearlyEqual.nearlyEqual(1.0 + 2.0, 3.0)).isTrue();
        assertThat(NearlyEqual.nearlyEqual(1.0, 1.1)).isFalse();
    }

    @Test
    void theToleranceIsTight() {
        assertThat(NearlyEqual.nearlyEqual(1.0, 1.000001)).isFalse();
        assertThat(NearlyEqual.nearlyEqual(1.0, 1.0 + 1e-10)).isTrue();
    }

    @Test
    void anInfinityEqualsItself() {
        assertThat(NearlyEqual.nearlyEqual(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isTrue();
        assertThat(NearlyEqual.nearlyEqual(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)).isTrue();
        assertThat(NearlyEqual.nearlyEqual(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)).isFalse();
    }

    @Test
    void nanEqualsNothing() {
        assertThat(NearlyEqual.nearlyEqual(Double.NaN, Double.NaN)).isFalse();
        assertThat(NearlyEqual.nearlyEqual(Double.NaN, 0.0)).isFalse();
    }
}
