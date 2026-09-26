package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QuadrantsTest {

    @Test
    void findsTheQuadrant() {
        assertThat(Quadrants.quadrant(new Quadrants.Point(2, 3))).isEqualTo(1);
        assertThat(Quadrants.quadrant(new Quadrants.Point(-2, 3))).isEqualTo(2);
        assertThat(Quadrants.quadrant(new Quadrants.Point(-2, -3))).isEqualTo(3);
        assertThat(Quadrants.quadrant(new Quadrants.Point(2, -3))).isEqualTo(4);
    }

    @Test
    void anAxisIsNoQuadrant() {
        assertThat(Quadrants.quadrant(new Quadrants.Point(0, 5))).isZero();
        assertThat(Quadrants.quadrant(new Quadrants.Point(-4, 0))).isZero();
        assertThat(Quadrants.quadrant(new Quadrants.Point(0, 0))).isZero();
    }

    @Test
    void otherValuesAreMinusOne() {
        assertThat(Quadrants.quadrant("2,3")).isEqualTo(-1);
        assertThat(Quadrants.quadrant(null)).isEqualTo(-1);
    }
}
