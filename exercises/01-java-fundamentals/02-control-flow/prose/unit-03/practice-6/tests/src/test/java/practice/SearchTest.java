package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SearchTest {

    @Test
    void findsAValueInside() {
        int[] sorted = {1, 3, 5, 7, 9, 11, 13};
        assertThat(Search.indexOf(sorted, 7)).isEqualTo(3);
        assertThat(Search.indexOf(sorted, 3)).isEqualTo(1);
        assertThat(Search.indexOf(sorted, 11)).isEqualTo(5);
    }

    @Test
    void findsTheValuesAtBothEnds() {
        int[] sorted = {1, 3, 5, 7, 9};
        assertThat(Search.indexOf(sorted, 1)).isZero();
        assertThat(Search.indexOf(sorted, 9)).isEqualTo(4);
        assertThat(Search.indexOf(new int[]{42}, 42)).isZero();
    }

    @Test
    void aMissingValueIsMinusOne() {
        int[] sorted = {1, 3, 5, 7, 9};
        assertThat(Search.indexOf(sorted, 4)).isEqualTo(-1);
        assertThat(Search.indexOf(sorted, 0)).isEqualTo(-1);
        assertThat(Search.indexOf(sorted, 10)).isEqualTo(-1);
        assertThat(Search.indexOf(new int[]{}, 4)).isEqualTo(-1);
    }
}
