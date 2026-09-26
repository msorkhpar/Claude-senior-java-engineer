package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimitiveOpsTest {

    @Test
    void worksOnPlainInts() {
        assertThat(PrimitiveOps.odds(new int[]{1, 2, 3, 4, 5, 6})).containsExactly(1, 3, 5);
        assertThat(PrimitiveOps.odds(new int[]{3, 3, 5})).containsExactly(3, 3, 5);
        assertThat(PrimitiveOps.padded(new int[]{1, 42, 999})).containsExactly("00001", "00042", "00999");
        assertThat(PrimitiveOps.padded(new int[]{-42, 123456})).containsExactly("-0042", "123456");
        assertThat(PrimitiveOps.totalLength(List.of("a", "ab", "abc"))).isEqualTo(6);
        assertThat(PrimitiveOps.totalLength(List.of("\u00e9t\u00e9"))).isEqualTo(3);
        assertThat(PrimitiveOps.product(new int[]{2, 3, 4})).isEqualTo(24);
    }

    @Test
    void negativeOddNumbersAreKept() {
        assertThat(PrimitiveOps.odds(new int[]{-3, -2, -1, 0, 1})).containsExactly(-3, -1, 1);
    }

    @Test
    void productOfNothingIsOne() {
        assertThat(PrimitiveOps.product(new int[]{})).isEqualTo(1);
    }
}
