package practice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColorTest {

    @Test
    void createsColorsFromPartsAndFromHex() {
        Color orange = Color.of(255, 128, 0);
        assertThat(orange.red()).isEqualTo(255);
        assertThat(orange.green()).isEqualTo(128);
        assertThat(orange.blue()).isZero();
        Color teal = Color.fromHex("#008080");
        assertThat(teal.red()).isZero();
        assertThat(teal.green()).isEqualTo(128);
        assertThat(teal.blue()).isEqualTo(128);
        Color lower = Color.fromHex("#1a2b3c");
        assertThat(lower.red()).isEqualTo(0x1a);
        assertThat(lower.green()).isEqualTo(0x2b);
        assertThat(lower.blue()).isEqualTo(0x3c);
    }

    @Test
    void equalPartsGiveTheSameInstance() {
        assertThat(Color.of(10, 20, 30)).isSameAs(Color.of(10, 20, 30));
        assertThat(Color.fromHex("#FF8000")).isSameAs(Color.of(255, 128, 0));
        assertThat(Color.of(10, 20, 30)).isNotSameAs(Color.of(30, 20, 10));
    }

    @Test
    void aPartOutsideTheRangeIsRefused() {
        assertThat(Color.of(0, 255, 0).green()).isEqualTo(255);
        assertThatThrownBy(() -> Color.of(256, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Color.of(0, -1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Color.of(0, 0, 300)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noConstructorIsPublic() {
        assertThat(Color.of(1, 2, 3).blue()).isEqualTo(3);
        Constructor<?>[] constructors = Color.class.getDeclaredConstructors();
        assertThat(constructors).isNotEmpty();
        for (Constructor<?> constructor : constructors) {
            assertThat(Modifier.isPrivate(constructor.getModifiers()))
                    .as("constructor %s is private", constructor)
                    .isTrue();
        }
    }
}
