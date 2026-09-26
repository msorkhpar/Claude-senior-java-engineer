package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SwapperTest {

    @Test
    void swapsTwoElements() {
        int[] values = {1, 2, 3};
        Swapper.swap(values, 0, 2);
        assertThat(values).containsExactly(3, 2, 1);
        int[] pair = {7, 9};
        Swapper.swap(pair, 1, 0);
        assertThat(pair).containsExactly(9, 7);
    }

    @Test
    void anElementSwappedWithItselfStays() {
        int[] values = {3, 2, 1};
        Swapper.swap(values, 1, 1);
        assertThat(values).containsExactly(3, 2, 1);
    }
}
