package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AreasTest {

    @Test
    void measuresEachShape() {
        assertThat(Areas.area(new Areas.Square(3))).isEqualTo(9.0);
        assertThat(Areas.area(new Areas.Circle(1))).isCloseTo(Math.PI, within(1e-9));
        assertThat(Areas.area(new Areas.Rectangle(2, 5))).isEqualTo(10.0);
    }

    @Test
    void anythingElseIsRefused() {
        assertThatThrownBy(() -> Areas.area("square")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Areas.area(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
