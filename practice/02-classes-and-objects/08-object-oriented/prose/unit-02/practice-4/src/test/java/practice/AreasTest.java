package practice;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AreasTest {

    private Locale saved;

    @BeforeEach
    void pinTheLocale() {
        saved = Locale.getDefault();
        Locale.setDefault(Locale.ROOT);
    }

    @AfterEach
    void restoreTheLocale() {
        Locale.setDefault(saved);
    }

    @Test
    void shapesAnswerThroughTheInterface() {
        Areas.Shape circle = new Areas.Circle(5);
        Areas.Shape rectangle = new Areas.Rectangle(4, 5);
        assertThat(circle.calculateArea()).isCloseTo(78.54, within(0.01));
        assertThat(rectangle.calculateArea()).isEqualTo(20.0);
        assertThat(rectangle.display()).isEqualTo("This is a shape with area: 20.00");
        assertThat(new Areas.Circle(1).display()).isEqualTo("This is a shape with area: 3.14");
    }

    @Test
    void aSquareRecordReplacesTheDefaultDisplay() {
        Areas.Shape square = new Areas.Square(3);
        assertThat(square.calculateArea()).isEqualTo(9.0);
        assertThat(square.display()).isEqualTo("A square with side 3.00");
    }

    @Test
    void largestPicksTheBiggestArea() {
        Areas.Shape circle = new Areas.Circle(3);
        List<Areas.Shape> shapes = List.of(circle, new Areas.Rectangle(3, 4), new Areas.Square(2));
        assertThat(Areas.largest(shapes)).containsSame(circle);
    }

    @Test
    void noShapesHaveNoLargest() {
        assertThat(Areas.largest(List.of())).isEmpty();
    }
}
