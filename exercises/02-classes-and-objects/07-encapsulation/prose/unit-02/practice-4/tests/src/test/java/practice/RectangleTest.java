package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RectangleTest {

    @Test
    void reportsItsSidesAreaAndShape() {
        Rectangle rectangle = new Rectangle(3, 4);
        assertThat(rectangle.getWidth()).isEqualTo(3.0);
        assertThat(rectangle.getHeight()).isEqualTo(4.0);
        assertThat(rectangle.getArea()).isEqualTo(12.0);
        assertThat(rectangle.isSquare()).isFalse();
        assertThat(new Rectangle(5, 5).isSquare()).isTrue();
    }

    @Test
    void theAreaFollowsASetter() {
        Rectangle rectangle = new Rectangle(3, 4);
        rectangle.setWidth(4);
        assertThat(rectangle.getArea()).isEqualTo(16.0);
        assertThat(rectangle.isSquare()).isTrue();
        rectangle.setHeight(2.5);
        assertThat(rectangle.getArea()).isEqualTo(10.0);
    }

    @Test
    void aNonPositiveSideIsRefused() {
        Rectangle rectangle = new Rectangle(3, 4);
        assertThatThrownBy(() -> rectangle.setHeight(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rectangle.setWidth(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Rectangle(0, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThat(rectangle.getWidth()).isEqualTo(3.0);
        assertThat(rectangle.getHeight()).isEqualTo(4.0);
    }
}
