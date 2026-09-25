package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class WholeNumbersTest {

    @Test
    void readsALong() {
        assertThat(WholeNumbers.toLong(5L)).isEqualTo(5L);
        assertThat(WholeNumbers.toLong(Long.MAX_VALUE)).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void readsTheSmallerWholeNumberTypes() {
        assertThat(WholeNumbers.toLong(Integer.valueOf(5))).isEqualTo(5L);
        assertThat(WholeNumbers.toLong(Short.valueOf((short) 7))).isEqualTo(7L);
        assertThat(WholeNumbers.toLong(Byte.valueOf((byte) -3))).isEqualTo(-3L);
    }

    @Test
    void refusesEverythingElse() {
        assertThatIllegalArgumentException().isThrownBy(() -> WholeNumbers.toLong(1.5));
        assertThatIllegalArgumentException().isThrownBy(() -> WholeNumbers.toLong("7"));
        assertThatIllegalArgumentException().isThrownBy(() -> WholeNumbers.toLong(null));
    }
}
