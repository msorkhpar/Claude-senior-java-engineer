package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EvensTest {

    @Test
    void keepsTheEvenNumbers() {
        assertThat(Evens.evens(new int[]{1, 2, 3, 4, 5, 6})).containsExactly(2, 4, 6);
        assertThat(Evens.evens(new int[]{2, 4})).containsExactly(2, 4);
    }

    @Test
    void keepsOrderAndDuplicates() {
        assertThat(Evens.evens(new int[]{6, 2, 2, 4})).containsExactly(6, 2, 2, 4);
    }

    @Test
    void negativeNumbersCount() {
        assertThat(Evens.evens(new int[]{-4, -3, 0, 7})).containsExactly(-4, 0);
        assertThat(Evens.evens(new int[]{-1, -5})).isEmpty();
    }

    @Test
    void noEvenNumbersGiveAnEmptyArray() {
        assertThat(Evens.evens(new int[]{1, 3, 5})).isNotNull().isEmpty();
        assertThat(Evens.evens(new int[]{})).isNotNull().isEmpty();
    }
}
