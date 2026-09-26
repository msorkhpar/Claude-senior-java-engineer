package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class FiguresTest {

    @Test
    void eachFigureAnswersBothQuestions() {
        Figures.Shape circle = new Figures.Circle(1);
        Figures.Shape square = new Figures.Square(2);
        Figures.Shape triangle = new Figures.Triangle(3, 4, 5);
        assertThat(circle.area()).isCloseTo(Math.PI, within(1e-9));
        assertThat(circle.perimeter()).isCloseTo(2 * Math.PI, within(1e-9));
        assertThat(square.area()).isCloseTo(4.0, within(1e-9));
        assertThat(square.perimeter()).isCloseTo(8.0, within(1e-9));
        assertThat(triangle.area()).isCloseTo(6.0, within(1e-9));
        assertThat(triangle.perimeter()).isCloseTo(12.0, within(1e-9));
    }

    @Test
    void impossibleTrianglesAreRefusedInAnyOrder() {
        assertThatThrownBy(() -> new Figures.Triangle(1, 2, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Figures.Triangle(10, 2, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Figures.Triangle(2, 10, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aFlatTriangleIsRefused() {
        assertThatThrownBy(() -> new Figures.Triangle(1, 2, 3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSquareNeedsAPositiveSide() {
        assertThatThrownBy(() -> new Figures.Square(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Figures.Square(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
