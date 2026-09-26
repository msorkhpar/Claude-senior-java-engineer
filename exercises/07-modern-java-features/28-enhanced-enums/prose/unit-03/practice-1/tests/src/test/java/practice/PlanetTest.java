package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class PlanetTest {

    @Test
    void computesGravityAndWeight() {
        assertThat(Planet.EARTH.surfaceGravity()).isCloseTo(9.8027, within(1e-3));
        assertThat(Planet.EARTH.surfaceWeight(75)).isCloseTo(735.199, within(1e-2));
        assertThat(Planet.MARS.surfaceGravity()).isCloseTo(3.7126, within(1e-3));
        assertThat(Planet.strongest(List.of(Planet.values()))).isEqualTo(Planet.JUPITER);
        assertThat(Planet.withGravityBetween(9, 12)).containsExactly(Planet.EARTH, Planet.SATURN, Planet.NEPTUNE);
    }

    @Test
    void strongestIsByGravityNotSize() {
        assertThat(Planet.strongest(List.of(Planet.URANUS, Planet.SATURN, Planet.NEPTUNE))).isEqualTo(Planet.NEPTUNE);
    }

    @Test
    void boundsAreInclusive() {
        double earth = Planet.EARTH.surfaceGravity();
        assertThat(Planet.withGravityBetween(earth, earth)).containsExactly(Planet.EARTH);
    }

    @Test
    void resultKeepsDeclarationOrder() {
        assertThat(Planet.withGravityBetween(8, 12))
                .containsExactly(Planet.VENUS, Planet.EARTH, Planet.SATURN, Planet.URANUS, Planet.NEPTUNE);
    }

    @Test
    void strongestOfNothingThrows() {
        assertThatThrownBy(() -> Planet.strongest(List.of())).isInstanceOf(NoSuchElementException.class);
    }
}
