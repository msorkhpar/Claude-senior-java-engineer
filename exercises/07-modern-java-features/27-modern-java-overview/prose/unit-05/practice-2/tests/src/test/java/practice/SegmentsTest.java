package practice;

import org.junit.jupiter.api.Test;

import practice.Segments.Line;
import practice.Segments.Point;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentsTest {

    private static Line line(int x1, int y1, int x2, int y2) {
        return new Line(new Point(x1, y1), new Point(x2, y2));
    }

    @Test
    void describesPointsAndLines() {
        assertThat(Segments.describe(new Point(0, 0))).isEqualTo("origin");
        assertThat(Segments.describe(new Point(3, -4))).isEqualTo("point (3, -4)");
        assertThat(Segments.describe(new Point(0, 7))).isEqualTo("point (0, 7)");
        assertThat(Segments.describe(new Point(5, 5))).isEqualTo("point (5, 5)");
        assertThat(Segments.describe(new Point(3, -3))).isEqualTo("point (3, -3)");
        assertThat(Segments.describe(line(2, 0, 2, 9))).isEqualTo("vertical at x=2");
        assertThat(Segments.describe(line(-1, 5, 8, 5))).isEqualTo("horizontal at y=5");
        assertThat(Segments.describe("a line")).isEqualTo("not a shape");
    }

    @Test
    void aLineOfOnePointIsDegenerate() {
        assertThat(Segments.describe(line(1, 2, 1, 2))).isEqualTo("degenerate at (1, 2)");
        assertThat(Segments.describe(line(300, 300, 300, 300))).isEqualTo("degenerate at (300, 300)");
    }

    @Test
    void aGeneralLineKeepsItsDirection() {
        assertThat(Segments.describe(line(0, 0, 3, 4))).isEqualTo("from (0, 0) to (3, 4)");
        assertThat(Segments.describe(line(9, 1, -2, 7))).isEqualTo("from (9, 1) to (-2, 7)");
    }
}
