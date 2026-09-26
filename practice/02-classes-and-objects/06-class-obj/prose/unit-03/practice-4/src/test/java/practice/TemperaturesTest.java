package practice;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class TemperaturesTest {

    @Test
    void convertsCelsius() {
        assertThat(Temperatures.toFahrenheit(100)).isCloseTo(212.0, within(1e-9));
        assertThat(Temperatures.toFahrenheit(-40)).isCloseTo(-40.0, within(1e-9));
        assertThat(Temperatures.toKelvin(0)).isCloseTo(273.15, within(1e-9));
        assertThat(Temperatures.toKelvin(25)).isCloseTo(298.15, within(1e-9));
    }

    @Test
    void belowAbsoluteZeroIsRefused() {
        assertThatThrownBy(() -> Temperatures.toFahrenheit(-300)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Temperatures.toKelvin(-273.16)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void absoluteZeroItselfIsAllowed() {
        assertThat(Temperatures.toKelvin(-273.15)).isCloseTo(0.0, within(1e-9));
        assertThat(Temperatures.toFahrenheit(-273.15)).isCloseTo(-459.67, within(1e-9));
    }

    @Test
    void theConstantIsPublicStaticFinal() throws Exception {
        Field constant = Temperatures.class.getField("ABSOLUTE_ZERO_CELSIUS");
        int modifiers = constant.getModifiers();
        assertThat(Modifier.isPublic(modifiers)).isTrue();
        assertThat(Modifier.isStatic(modifiers)).isTrue();
        assertThat(Modifier.isFinal(modifiers)).isTrue();
        assertThat(constant.getDouble(null)).isEqualTo(-273.15);
    }
}
