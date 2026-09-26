package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShapesTest {

    @Test
    void describesARectangle() {
        assertThat(Shapes.describe(new Shapes.Rectangle(new Shapes.Point(1, 2), new Shapes.Point(3, 4)))).isEqualTo("Rectangle from (1,2) to (3,4)");
        assertThat(Shapes.describe("square")).isEqualTo("Unknown shape");
    }

    @Test
    void describesACircle() {
        assertThat(Shapes.describe(new Shapes.Circle(new Shapes.Point(0, 0), 5))).isEqualTo("Circle at (0,0) with radius 5");
        assertThat(Shapes.describe(new Shapes.Circle(new Shapes.Point(-1, 2), 3))).isEqualTo("Circle at (-1,2) with radius 3");
    }

    @Test
    void nullHasItsOwnCase() {
        assertThat(Shapes.describe(null)).isEqualTo("Null shape");
    }
}
