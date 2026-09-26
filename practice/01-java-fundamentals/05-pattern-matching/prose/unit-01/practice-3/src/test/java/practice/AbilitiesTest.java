package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AbilitiesTest {

    @Test
    void namesTheInterface() {
        assertThat(Abilities.of(new Thread())).containsExactly("Runnable");
        assertThat(Abilities.of(42)).containsExactly("Comparable");
        assertThat(Abilities.of(new Object())).isEmpty();
    }

    @Test
    void oneObjectCanHaveSeveral() {
        assertThat(Abilities.of("hi")).containsExactly("Comparable", "CharSequence");
    }

    @Test
    void nullHasNone() {
        assertThat(Abilities.of(null)).isEmpty();
    }
}
