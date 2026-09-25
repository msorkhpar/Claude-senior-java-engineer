package practice;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ShapesTest {

    @Test
    void computesAndDescribesEachShape() {
        assertThat(new Shapes.Circle(1).area()).isCloseTo(Math.PI, within(1e-9));
        assertThat(new Shapes.Square(4).area()).isEqualTo(16.0);
        assertThat(new Shapes.Triangle(3, 4).area()).isEqualTo(6.0);
        assertThat(Shapes.describe(new Shapes.Circle(2))).isEqualTo("circle of radius 2.0");
        assertThat(Shapes.describe(new Shapes.Square(4))).isEqualTo("square of side 4.0");
        assertThat(Shapes.describe(new Shapes.Triangle(3, 4))).isEqualTo("triangle of area 6.0");
    }

    @Test
    void shapeIsSealedToExactlyThreeKinds() {
        assertThat(Shapes.Shape.class.isSealed()).isTrue();
        assertThat(Shapes.Shape.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(Shapes.Circle.class, Shapes.Square.class, Shapes.Triangle.class);
    }

    @Test
    void eachSubclassSaysHowTheHierarchyGoesOn() {
        assertThat(Modifier.isFinal(Shapes.Circle.class.getModifiers())).isTrue();
        assertThat(Modifier.isFinal(Shapes.Square.class.getModifiers())).isTrue();
        assertThat(Modifier.isFinal(Shapes.Triangle.class.getModifiers())).isFalse();
        assertThat(Shapes.Triangle.class.isSealed()).isFalse();
    }
}
