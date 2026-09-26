package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class PointTest {

    @Test
    void parsesAndMeasuresAPoint() {
        Point p = Point.parse("3,4");
        assertThat(p).isEqualTo(new Point(3, 4));
        assertThat(p.distanceFromOrigin()).isCloseTo(5.0, within(1e-9));
        assertThat(Point.ORIGIN).isEqualTo(new Point(0, 0));
        assertThat(Point.ORIGIN.distanceFromOrigin()).isZero();
    }

    @Test
    void farPointsDoNotOverflow() {
        assertThat(new Point(3_000_000, 4_000_000).distanceFromOrigin()).isCloseTo(5_000_000.0, within(1e-6));
        assertThat(new Point(-50_000, 0).distanceFromOrigin()).isCloseTo(50_000.0, within(1e-6));
    }

    @Test
    void parseAcceptsSpacesAndSigns() {
        assertThat(Point.parse(" -3 , 4 ")).isEqualTo(new Point(-3, 4));
    }

    @Test
    void malformedTextIsRefused() {
        assertThatThrownBy(() -> Point.parse("3;4")).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Point.parse("1,2,3")).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Point.parse("a,1")).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
