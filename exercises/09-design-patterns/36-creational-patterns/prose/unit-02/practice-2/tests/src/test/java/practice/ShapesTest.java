package practice;

import org.junit.jupiter.api.Test;

import practice.Shapes.Circle;
import practice.Shapes.Shape;
import practice.Shapes.Square;
import practice.Shapes.Triangle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ShapesTest {

    @Test
    void createsEachKnownShape() {
        Shape circle = Shapes.create("circle", 2);
        Shape square = Shapes.create("square", 3);
        Shape triangle = Shapes.create("triangle", 4);

        assertThat(circle).isEqualTo(new Circle(2));
        assertThat(circle.area()).isCloseTo(12.566370614359172, within(1e-9));
        assertThat(square).isEqualTo(new Square(3));
        assertThat(square.area()).isEqualTo(9.0);
        assertThat(triangle).isEqualTo(new Triangle(4, 4));
        assertThat(triangle.area()).isEqualTo(8.0);
        assertThat(Shapes.describe(circle)).isEqualTo("Round r=2.0");
    }

    @Test
    void anUnknownTypeIsRefusedByName() {
        assertThatThrownBy(() -> Shapes.create("hexagon", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hexagon");
        assertThatThrownBy(() -> Shapes.create("Circle", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Circle");
    }

    @Test
    void aTypeBuiltAtRunTimeIsFound() {
        String built = new StringBuilder("sq").append("uare").toString();

        assertThat(Shapes.create(built, 5)).isEqualTo(new Square(5));
    }

    @Test
    void describesEachShape() {
        assertThat(Shapes.describe(new Circle(1.5))).isEqualTo("Round r=1.5");
        assertThat(Shapes.describe(new Square(3))).isEqualTo("Square s=3.0");
        assertThat(Shapes.describe(new Triangle(2, 7))).isEqualTo("Triangle b=2.0 h=7.0");
    }
}
