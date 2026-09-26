package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InitialTest {

    @Test
    void givesTheInitial() {
        assertThat(Initial.of("hello")).isEqualTo('H');
        assertThat(Initial.of("Ada")).isEqualTo('A');
        assertThat(Initial.of(42)).isEqualTo('?');
    }

    @Test
    void anEmptyStringHasNoInitial() {
        assertThat(Initial.of("")).isEqualTo('?');
    }

    @Test
    void nullHasNoInitial() {
        assertThat(Initial.of(null)).isEqualTo('?');
    }
}
