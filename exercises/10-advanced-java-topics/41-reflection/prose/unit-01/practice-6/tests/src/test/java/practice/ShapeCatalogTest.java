package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShapeCatalogTest {

    record Point(int x, int y) {
    }

    record Span(int end, int begin) {
    }

    static final class Pair {
        final int left;
        final String right;

        Pair(int left, String right) {
            this.left = left;
            this.right = right;
        }
    }

    sealed interface Shape permits Circle, Rect {
    }

    record Circle(double radius) implements Shape {
    }

    record Rect(double width, double height) implements Shape {
    }

    sealed interface Figure permits Dot, Polygon {
    }

    record Dot() implements Figure {
    }

    sealed interface Polygon extends Figure permits Square, Triangle {
    }

    record Square(double side) implements Polygon {
    }

    record Triangle(double base, double height) implements Polygon {
    }

    @Test
    void readsARecordAndASealedInterface() {
        assertThat(ShapeCatalog.components(Point.class)).containsExactly("x: int", "y: int");
        assertThat(ShapeCatalog.leaves(Shape.class)).containsExactly("Circle", "Rect");
        assertThatThrownBy(() -> ShapeCatalog.leaves(Runnable.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void componentsKeepDeclarationOrder() {
        assertThat(ShapeCatalog.components(Span.class)).containsExactly("end: int", "begin: int");
    }

    @Test
    void aClassThatIsNotARecordIsRefused() {
        assertThatThrownBy(() -> ShapeCatalog.components(Pair.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nestedSealedTypesAreExpanded() {
        assertThat(ShapeCatalog.leaves(Figure.class)).containsExactly("Dot", "Square", "Triangle");
    }
}
