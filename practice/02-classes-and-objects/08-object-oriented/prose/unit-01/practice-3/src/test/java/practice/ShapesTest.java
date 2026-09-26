package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ShapesTest {

    @Test
    void eachShapeComputesItsOwnArea() {
        Shapes.Shape circle = new Shapes.Circle("Red", 5.0);
        Shapes.Shape rectangle = new Shapes.Rectangle("Blue", 4.0, 5.0);
        assertThat(circle.calculateArea()).isCloseTo(78.54, within(0.01));
        assertThat(rectangle.calculateArea()).isEqualTo(20.0);
        assertThat(circle.displayColor()).isEqualTo("The shape color is Red");
        assertThat(rectangle.displayColor()).isEqualTo("The shape color is Blue");
        List<Shapes.Shape> shapes = List.of(new Shapes.Circle("Green", 3.0), new Shapes.Rectangle("Yellow", 2.0, 3.0));
        assertThat(shapes.get(0).calculateArea()).isCloseTo(28.27, within(0.01));
        assertThat(shapes.get(1).calculateArea()).isEqualTo(6.0);
        assertThat(Shapes.totalArea(shapes)).isCloseTo(34.27, within(0.01));
    }

    @Test
    void toStringNamesTheShapeAndItsFields() {
        assertThat(new Shapes.Circle("Red", 5.0).toString()).contains("Circle", "Red", "5.0");
        assertThat(new Shapes.Rectangle("Blue", 4.0, 5.0).toString()).contains("Rectangle", "Blue", "4.0", "5.0");
    }

    @Test
    void noShapesHaveNoArea() {
        assertThat(Shapes.totalArea(List.of())).isEqualTo(0.0);
    }

    @Test
    void negativeDimensionsAreRefused() {
        assertThatThrownBy(() -> new Shapes.Circle("Red", -1.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Shapes.Rectangle("Blue", 2.0, -3.0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new Shapes.Circle("Red", 0.0).calculateArea()).isEqualTo(0.0);
    }
}
