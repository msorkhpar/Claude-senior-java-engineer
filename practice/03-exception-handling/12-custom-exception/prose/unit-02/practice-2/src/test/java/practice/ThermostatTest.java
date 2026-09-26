package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThermostatTest {

    @Test
    void acceptsATargetInRange() {
        Thermostat t = new Thermostat();
        assertThat(t.getTarget()).isEqualTo(20);
        t.setTarget(22);
        t.setTarget(5);
        t.setTarget(30);
        assertThat(t.getTarget()).isEqualTo(30);
        assertThat(t.history()).containsExactly(22, 5, 30);
        assertThatThrownBy(() -> t.setTarget(31)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> t.setTarget(4)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theMessageStatesTheRejectedValue() {
        Thermostat t = new Thermostat();
        assertThatThrownBy(() -> t.setTarget(42))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target out of range: 42");
    }

    @Test
    void aRejectedTargetChangesNothing() {
        Thermostat t = new Thermostat();
        t.setTarget(22);
        assertThatThrownBy(() -> t.setTarget(42)).isInstanceOf(IllegalArgumentException.class);
        assertThat(t.getTarget()).isEqualTo(22);
        assertThat(t.history()).containsExactly(22);
    }

    @Test
    void aNullTargetIsNamedInTheMessage() {
        Thermostat t = new Thermostat();
        assertThatThrownBy(() -> t.setTarget(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("celsius");
        assertThat(t.history()).isEmpty();
    }
}
