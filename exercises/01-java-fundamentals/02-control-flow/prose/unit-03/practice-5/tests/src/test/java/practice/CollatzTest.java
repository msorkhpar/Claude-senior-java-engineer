package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CollatzTest {

    @Test
    void countsTheSteps() {
        assertThat(Collatz.steps(6, 100)).isEqualTo(8);
        assertThat(Collatz.steps(16, 100)).isEqualTo(4);
        assertThat(Collatz.steps(27, 111)).isEqualTo(111);
    }

    @Test
    void oneNeedsNoStep() {
        assertThat(Collatz.steps(1, 100)).isZero();
        assertThat(Collatz.steps(1, 0)).isZero();
    }

    @Test
    void tooManyStepsAreRefused() {
        assertThatThrownBy(() -> Collatz.steps(27, 100)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Collatz.steps(6, 7)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aStartBelowOneIsRefused() {
        assertThatThrownBy(() -> Collatz.steps(0, 100)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Collatz.steps(-4, 100)).isInstanceOf(IllegalArgumentException.class);
    }
}
