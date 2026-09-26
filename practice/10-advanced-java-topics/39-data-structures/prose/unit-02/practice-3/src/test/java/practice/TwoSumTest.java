package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoSumTest {

    @Test
    void findsThePairThatAddsUp() {
        assertThat(TwoSum.find(new int[] {2, 7, 11, 15}, 9)).containsExactly(0, 1);
        assertThat(TwoSum.find(new int[] {-3, 4, 3, 90}, 0)).containsExactly(0, 2);
        assertThat(TwoSum.find(new int[] {1000, 5, 2000, 7}, 3000)).containsExactly(0, 2);
    }

    @Test
    void anElementNeverPairsWithItself() {
        assertThat(TwoSum.find(new int[] {3, 2, 4}, 6)).containsExactly(1, 2);
    }

    @Test
    void twoEqualValuesCanPair() {
        assertThat(TwoSum.find(new int[] {3, 3}, 6)).containsExactly(0, 1);
        assertThat(TwoSum.find(new int[] {500, 1, 500}, 1000)).containsExactly(0, 2);
    }

    @Test
    void noPairGivesAnEmptyArray() {
        assertThat(TwoSum.find(new int[] {1, 2, 3}, 100)).isNotNull().isEmpty();
        assertThat(TwoSum.find(new int[0], 5)).isNotNull().isEmpty();
    }
}
