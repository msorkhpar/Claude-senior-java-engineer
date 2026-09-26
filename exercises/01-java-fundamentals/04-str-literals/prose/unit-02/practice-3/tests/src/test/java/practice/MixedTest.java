package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MixedTest {

    @Test
    void describesTheValues() {
        assertThat(Mixed.describe(42, true, 3.14)).isEqualTo("Number: 42, Flag: true, Value: 3.14");
        assertThat(Mixed.describe(-1, false, 0.5)).isEqualTo("Number: -1, Flag: false, Value: 0.5");
    }

    @Test
    void numbersAreAddedBeforeJoining() {
        assertThat(Mixed.total("Sum", 2, 3)).isEqualTo("Sum: 5");
        assertThat(Mixed.total("Net", 10, -4)).isEqualTo("Net: 6");
    }

    @Test
    void charsAreJoinedAsText() {
        assertThat(Mixed.initials('A', 'B')).isEqualTo("AB");
        assertThat(Mixed.initials('g', 'h')).isEqualTo("gh");
    }
}
