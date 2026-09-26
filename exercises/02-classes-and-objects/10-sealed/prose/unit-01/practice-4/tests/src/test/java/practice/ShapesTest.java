package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShapesTest {

    @Test
    void describesEachPermittedShape() {
        assertThat(Shapes.describe(new Shapes.Circle(5))).isEqualTo("A circle with radius 5.0");
        assertThat(Shapes.describe(new Shapes.Square(4))).isEqualTo("A square with side length 4.0");
        assertThat(Shapes.describe(new Shapes.Triangle(3, 4)))
                .isEqualTo("A triangle with base 3.0 and height 4.0");
    }

    @Test
    void aRightTriangleIsDescribedAsATriangle() {
        assertThat(Shapes.describe(new Shapes.RightTriangle(3, 4)))
                .isEqualTo("A triangle with base 3.0 and height 4.0");
    }
}
