package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeometryTest {

    @Test
    void measuresPointsAndLines() {
        assertThat(Geometry.gridLength(new Geometry.Point(2, 9))).isZero();
        assertThat(Geometry.gridLength(new Geometry.Line(new Geometry.Point(0, 0), new Geometry.Point(3, 4))))
                .isEqualTo(7);
        assertThat(Geometry.gridLength(new Geometry.Box(new Geometry.Point(0, 0), new Geometry.Point(2, 3))))
                .isEqualTo(10);
        assertThat(Geometry.gridLength("a circle")).isEqualTo(-1);
    }

    @Test
    void linesRunningBackwardsCountToo() {
        assertThat(Geometry.gridLength(new Geometry.Line(new Geometry.Point(3, 4), new Geometry.Point(0, 0))))
                .isEqualTo(7);
        assertThat(Geometry.gridLength(new Geometry.Line(new Geometry.Point(0, 5), new Geometry.Point(5, 0))))
                .isEqualTo(10);
    }

    @Test
    void aBoxIsItsPerimeter() {
        assertThat(Geometry.gridLength(new Geometry.Box(new Geometry.Point(2, 3), new Geometry.Point(0, 0))))
                .isEqualTo(10);
        assertThat(Geometry.gridLength(new Geometry.Box(new Geometry.Point(0, 3), new Geometry.Point(2, 0))))
                .isEqualTo(10);
    }

    @Test
    void nullAndOtherObjectsAreMinusOne() {
        assertThat(Geometry.gridLength(null)).isEqualTo(-1);
        assertThat(Geometry.gridLength(42)).isEqualTo(-1);
    }
}
