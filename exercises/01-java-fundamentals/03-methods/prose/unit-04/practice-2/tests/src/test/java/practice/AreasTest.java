package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AreasTest {

    @Test
    void measuresEachShape() {
        assertThat(Areas.area(1.0)).isCloseTo(Math.PI, within(1e-9));
        assertThat(Areas.area(2.0)).isCloseTo(4 * Math.PI, within(1e-9));
        assertThat(Areas.area(2.0, 3.5)).isEqualTo(7.0);
    }

    @Test
    void anIntSideIsASquare() {
        assertThat(Areas.area(4)).isEqualTo(16.0);
        assertThat(Areas.area(Integer.valueOf(3))).isEqualTo(9.0);
    }

    @Test
    void aLargeSideDoesNotOverflow() {
        assertThat(Areas.area(100000)).isEqualTo(1.0e10);
    }
}
