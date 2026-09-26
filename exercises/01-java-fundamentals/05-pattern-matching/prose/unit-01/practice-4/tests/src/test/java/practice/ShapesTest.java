package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShapesTest {

    /** A shape the solution has never seen. */
    record Square(double side) implements Shapes.Shape {
        @Override
        public double area() {
            return side * side;
        }
    }

    @Test
    void addsUpKnownShapes() {
        assertThat(new Shapes.Circle(1).area()).isCloseTo(Math.PI, within(1e-9));
        assertThat(new Shapes.Rectangle(4, 6).area()).isEqualTo(24.0);
        assertThat(Shapes.total(List.of(new Shapes.Rectangle(4, 6), new Shapes.Rectangle(1, 2)))).isEqualTo(26.0);
        assertThat(Shapes.total(List.of())).isZero();
    }

    @Test
    void aNewKindOfShapeCounts() {
        assertThat(Shapes.total(List.of(new Square(3), new Shapes.Rectangle(1, 1)))).isEqualTo(10.0);
    }
}
