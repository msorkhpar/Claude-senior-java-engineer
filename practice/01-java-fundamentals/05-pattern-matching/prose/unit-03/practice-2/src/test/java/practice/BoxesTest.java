package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BoxesTest {

    @Test
    void measuresARectangle() {
        assertThat(Boxes.area(new Boxes.Rectangle(new Boxes.Point(1, 2), new Boxes.Point(3, 5)))).isEqualTo(6);
        assertThat(Boxes.area(new Boxes.Rectangle(new Boxes.Point(0, 0), new Boxes.Point(1, 1)))).isEqualTo(1);
        assertThat(Boxes.area("box")).isEqualTo(-1);
    }

    @Test
    void cornersMayComeInEitherOrder() {
        assertThat(Boxes.area(new Boxes.Rectangle(new Boxes.Point(3, 5), new Boxes.Point(1, 2)))).isEqualTo(6);
        assertThat(Boxes.area(new Boxes.Rectangle(new Boxes.Point(0, 4), new Boxes.Point(2, 0)))).isEqualTo(8);
    }

    @Test
    void aMissingCornerDoesNotMatch() {
        assertThat(Boxes.area(new Boxes.Rectangle(null, new Boxes.Point(1, 1)))).isEqualTo(-1);
        assertThat(Boxes.area(null)).isEqualTo(-1);
    }
}
