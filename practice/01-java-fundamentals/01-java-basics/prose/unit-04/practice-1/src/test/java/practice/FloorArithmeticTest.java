package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FloorArithmeticTest {

    @Test
    void worksForNonNegativeInputs() {
        assertThat(FloorArithmetic.wrap(7, 5)).isEqualTo(2);
        assertThat(FloorArithmetic.wrap(3, 5)).isEqualTo(3);
        assertThat(FloorArithmetic.wrap(0, 5)).isEqualTo(0);
        assertThat(FloorArithmetic.bucket(25, 10)).isEqualTo(2);
        assertThat(FloorArithmetic.bucket(9, 10)).isEqualTo(0);
        assertThat(FloorArithmetic.bucket(0, 10)).isEqualTo(0);
    }

    @Test
    void aNegativeIndexCountsBackFromTheEnd() {
        assertThat(FloorArithmetic.wrap(-1, 5)).isEqualTo(4);
        assertThat(FloorArithmetic.wrap(-11, 5)).isEqualTo(4);
        assertThat(FloorArithmetic.wrap(-5, 5)).isEqualTo(0);
    }

    @Test
    void aNegativeValueFallsInANegativeBucket() {
        assertThat(FloorArithmetic.bucket(-1, 10)).isEqualTo(-1);
        assertThat(FloorArithmetic.bucket(-10, 10)).isEqualTo(-1);
        assertThat(FloorArithmetic.bucket(-11, 10)).isEqualTo(-2);
    }
}
