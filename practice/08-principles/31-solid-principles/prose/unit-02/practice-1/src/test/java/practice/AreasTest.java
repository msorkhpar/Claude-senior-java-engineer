package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AreasTest {

    /** A shape the calculator has never heard of; its name is not its class name. */
    record Square(double side) implements Areas.Shape {
        public double area() { return side * side; }
        public String name() { return "Box"; }
    }

    @Test
    void totalsTheBuiltInShapes() {
        var calc = new Areas.AreaCalculator();
        List<Areas.Shape> shapes = List.of(new Areas.Circle(1), new Areas.Rectangle(2, 3), new Areas.Triangle(4, 5));
        assertThat(calc.totalArea(shapes)).isCloseTo(Math.PI + 6 + 10, within(1e-9));
        assertThat(calc.areaByType(List.of(new Areas.Rectangle(2, 3), new Areas.Circle(1), new Areas.Rectangle(1, 1))))
                .containsOnlyKeys("Circle", "Rectangle")
                .hasEntrySatisfying("Circle", a -> assertThat(a).isCloseTo(Math.PI, within(1e-9)))
                .hasEntrySatisfying("Rectangle", a -> assertThat(a).isCloseTo(7.0, within(1e-9)));
        assertThat(new Areas.Circle(2).name()).isEqualTo("Circle");
        assertThat(new Areas.Rectangle(2, 2).name()).isEqualTo("Rectangle");
        assertThat(new Areas.Triangle(2, 2).name()).isEqualTo("Triangle");
        assertThat(calc.areaByType(List.of(new Areas.Triangle(4, 5), new Areas.Triangle(2, 2)))).containsEntry("Triangle", 12.0);
    }

    @Test
    void aNewShapeNeedsNoChangeToTheCalculator() {
        var calc = new Areas.AreaCalculator();
        List<Areas.Shape> shapes = List.of(new Square(3), new Areas.Rectangle(2, 3));
        assertThat(calc.totalArea(shapes)).isCloseTo(15.0, within(1e-9));
        assertThat(calc.areaByType(shapes)).containsEntry("Box", 9.0).containsEntry("Rectangle", 6.0);
    }

    @Test
    void noShapesHaveNoArea() {
        var calc = new Areas.AreaCalculator();
        assertThat(calc.totalArea(List.of())).isEqualTo(0.0);
        assertThat(calc.areaByType(List.of())).isEmpty();
    }

    @Test
    void negativeDimensionsAreRejectedAtCreation() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Areas.Circle(-1));
        assertThatIllegalArgumentException().isThrownBy(() -> new Areas.Rectangle(2, -3));
        assertThatIllegalArgumentException().isThrownBy(() -> new Areas.Triangle(-4, 5));
        assertThatIllegalArgumentException().isThrownBy(() -> new Areas.Rectangle(-2, 3));
        assertThatIllegalArgumentException().isThrownBy(() -> new Areas.Triangle(4, -5));
        assertThat(new Areas.Rectangle(2, 3).area()).isEqualTo(6.0);
    }

    @Test
    void aZeroSizedShapeIsValid() {
        assertThat(new Areas.Circle(0).area()).isEqualTo(0.0);
        assertThat(new Areas.Rectangle(0, 3).area()).isEqualTo(0.0);
        assertThat(new Areas.AreaCalculator().totalArea(List.of(new Areas.Triangle(0, 0), new Areas.Rectangle(1, 2)))).isEqualTo(2.0);
    }
}
