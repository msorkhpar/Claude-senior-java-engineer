package practice;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class NumbersTest {

    @Test
    void addsNumbersOfAnyKind() {
        assertThat(Numbers.sum(List.of(1, 2.5, 3L))).isEqualTo(6.5);
        assertThat(Numbers.sum(List.of())).isZero();
    }

    @Test
    void otherItemsAreIgnored() {
        assertThat(Numbers.sum(List.of(1, "two", 3))).isEqualTo(4.0);
        assertThat(Numbers.sum(List.of("x", 'c', true))).isZero();
    }

    @Test
    void nullItemsAreIgnored() {
        assertThat(Numbers.sum(Arrays.asList(1, null, 2))).isEqualTo(3.0);
    }
}
