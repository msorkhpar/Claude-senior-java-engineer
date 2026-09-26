package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointTest {

    @Test
    void comparesCoordinates() {
        assertThat(new Point(1, 2).equals(new Point(1, 2))).isTrue();
        assertThat(new Point(1, 2).equals(new Point(2, 1))).isFalse();
        assertThat(new Point(0, 0).equals(new Point(0, 1))).isFalse();
    }

    @Test
    void nullIsNotAPoint() {
        assertThat(new Point(1, 2).equals(null)).isFalse();
    }

    @Test
    void anotherTypeIsNotAPoint() {
        assertThat(new Point(1, 2).equals("1,2")).isFalse();
        assertThat(new Point(1, 2).equals(12)).isFalse();
    }

    @Test
    void equalPointsHashAlike() {
        assertThat(new Point(1, 2).hashCode()).isEqualTo(new Point(1, 2).hashCode());
        assertThat(new Point(7, -3).hashCode()).isEqualTo(new Point(7, -3).hashCode());
    }
}
