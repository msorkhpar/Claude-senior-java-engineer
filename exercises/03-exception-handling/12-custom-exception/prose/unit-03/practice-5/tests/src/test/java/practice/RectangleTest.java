package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RectangleTest {

    @Test
    void buildsAndResizes() {
        Rectangle r = new Rectangle(2, 3);
        assertThat(r.getWidth()).isEqualTo(2);
        assertThat(r.getHeight()).isEqualTo(3);
        r.resize(4, 5);
        assertThat(r.getWidth()).isEqualTo(4);
        assertThat(r.getHeight()).isEqualTo(5);
        assertThatThrownBy(() -> r.resize(-1, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Width must be positive: -1");
    }

    @Test
    void aHalfValidResizeChangesNothing() {
        Rectangle r = new Rectangle(2, 3);
        assertThatThrownBy(() -> r.resize(10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Height must be positive: 0");
        assertThat(r.getWidth()).isEqualTo(2);
        assertThat(r.getHeight()).isEqualTo(3);
    }

    @Test
    void anInvalidRectangleCannotBeBuilt() {
        assertThatThrownBy(() -> new Rectangle(0, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Width must be positive: 0");
        assertThatThrownBy(() -> new Rectangle(3, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Height must be positive: -2");
    }

    @Test
    void theWidthIsCheckedFirst() {
        assertThatThrownBy(() -> new Rectangle(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Width must be positive: 0");
        Rectangle r = new Rectangle(2, 3);
        assertThatThrownBy(() -> r.resize(-4, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Width must be positive: -4");
    }
}
